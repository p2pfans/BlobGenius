package com.toipr.service.cache.impl;

import com.toipr.service.cache.CacheSubscriber;
import com.toipr.service.cache.JedisPoolServices;
import com.toipr.service.cache.MessageHandler;
import com.toipr.util.threads.ThreadPoolWorker;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DefaultCacheSubscriber extends JedisPubSub implements CacheSubscriber {
    protected class SubscribeEntry{
        public String tid;
        public String name;
        public boolean isChannel;
        public MessageHandler handler;
    }

    protected class SubscribeWorker implements Runnable {
        protected String[] channels;
        protected String[] patterns;

        protected boolean isExit = false;

        protected JedisPool pool;
        protected JedisPubSub subscriber;
        public SubscribeWorker(JedisPool pool, int dbIndex, JedisPubSub subscriber){
            this.pool = pool;
            this.subscriber = subscriber;
        }

        public void close(){
            isExit = true;
        }

        public void setChannels(String[] channels){
            this.channels = channels;
        }
        public void setPatterns(String[] patterns){
            this.patterns = patterns;
        }

        @Override
        public void run(){
            while(!isExit){
                Jedis client = jedisPool.getResource();
                if(client==null){
                    continue;
                }
                if(dbIndex>=0) {
                    client.select(dbIndex);
                }

                if(channels!=null && channels.length>0) {
                    client.subscribe(subscriber, channels);
                }

                if(patterns!=null && patterns.length>0) {
                    client.psubscribe(subscriber, patterns);
                }
                client.close();
            }
        }
    }

    protected Object lockObj = new Object();

    protected int dbIndex = -1;
    protected JedisPool jedisPool;
    protected SubscribeWorker channelWorker;
    protected SubscribeWorker patternWorker;

    protected List<String> lstChannel = new ArrayList<String>();
    protected List<String> lstPattern = new ArrayList<String>();

    protected List<SubscribeEntry> lstEntry = new ArrayList<SubscribeEntry>();
    protected Map<String, SubscribeEntry> mapEntry = new HashMap<String, SubscribeEntry>();

    protected Map<String, Object> mapChannels = new HashMap<String, Object>();
    protected Map<String, Object> mapPatterns = new HashMap<String, Object>();

    public DefaultCacheSubscriber(String host, int dbIndex){
        this.dbIndex = dbIndex;
        this.jedisPool = JedisPoolServices.getJedisPool(host);
    }

    /**
     * 关闭订阅管理器
     */
    public void close(){
        if(channelWorker!=null){
            channelWorker.close();
            channelWorker = null;
        }
        if(patternWorker!=null){
            patternWorker.close();
            patternWorker  = null;
        }
    }

    /**
     * 取消订阅
     * @param tid 任务ID
     */
    public void unsubscribe(String tid){
        synchronized (lockObj) {
            if(!mapEntry.containsKey(tid)){
                return;
            }

            List<SubscribeEntry> plist = null;
            SubscribeEntry item = mapEntry.remove(tid);
            if (item.isChannel) {
                lstChannel.remove(item.name);
                plist = (List<SubscribeEntry>)mapChannels.get(item.name);
                channelWorker.setChannels(toArray(lstChannel));
            } else {
                lstPattern.remove(item.name);
                plist = (List<SubscribeEntry>)mapPatterns.get(item.name);
                patternWorker.setPatterns(toArray(lstPattern));
            }
            plist.remove(item);
            lstEntry.remove(item);
        }
    }

    /**
     * 订阅消息频道channel
     * @param name 频道名称
     * @param isChannel true=频道名称 false=名称模式
     * @param handler 消息处理器
     * @return 成功=订阅ID 失败=null
     */
    public String subscribe(String name, boolean isChannel, MessageHandler handler){
        synchronized (lockObj) {
            SubscribeEntry item = null;
            for (int i = 0; i < lstEntry.size(); i++) {
                SubscribeEntry temp = lstEntry.get(i);
                if (temp.name.compareTo(name) == 0 && temp.isChannel == isChannel) {
                    item = temp;
                    break;
                }
            }
            if (item != null) {
                item.handler = handler;
                return item.tid;
            }

            item = new SubscribeEntry();
            item.tid = UUID.randomUUID().toString();
            item.name = name;
            item.isChannel = isChannel;
            item.handler = handler;

            List<SubscribeEntry> plist = null;
            if (isChannel) {
                lstChannel.add(name);
                if(mapChannels.containsKey(name)){
                    plist = (List<SubscribeEntry>)mapChannels.get(name);
                } else {
                    plist = new ArrayList<SubscribeEntry>();
                    mapChannels.put(name, plist);
                }
            } else {
                lstPattern.add(name);
                if(mapPatterns.containsKey(name)){
                    plist = (List<SubscribeEntry>)mapPatterns.get(name);
                } else {
                    plist = new ArrayList<SubscribeEntry>();
                    mapPatterns.put(name, plist);
                }
            }
            plist.add(item);

            mapEntry.put(item.tid, item);
            lstEntry.add(item);
            initWorker(isChannel);
            return item.tid;
        }
    }

    protected void initWorker(boolean isChannel){
        SubscribeWorker worker = null;
        if(isChannel && channelWorker==null){
            worker = new SubscribeWorker(jedisPool, dbIndex, this);
            worker.setChannels(toArray(lstChannel));
            channelWorker = worker;
        }

        if(!isChannel && patternWorker==null){
            worker = new SubscribeWorker(jedisPool, dbIndex, this);
            worker.setPatterns(toArray(lstPattern));
            patternWorker = worker;
        }

        if(worker!=null){
            Thread tobj = new Thread(worker);
            tobj.start();
        }
    }

    protected String[] toArray(List<String> plist){
        String[] sarr = new String[plist.size()];
        for(int i=0; i<sarr.length; i++){
            sarr[i] = plist.get(i);
        }
        return sarr;
    }

    @Override
    public void onMessage(String channel, String message) {
        List<SubscribeEntry> plist;
        synchronized (lockObj) {
            if (!mapChannels.containsKey(channel)) {
                return;
            }
            plist = (List<SubscribeEntry>)mapChannels.get(channel);
        }

        for(SubscribeEntry item : plist) {
            item.handler.onMessage(item.tid, channel, message);
        }
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        List<SubscribeEntry> plist;
        synchronized (lockObj) {
            if (!mapPatterns.containsKey(pattern)) {
                return;
            }
            plist = (List<SubscribeEntry>)mapPatterns.get(channel);
        }

        for(SubscribeEntry item : plist) {
            item.handler.onMessage(item.tid, pattern, channel, message);
        }
    }
}
