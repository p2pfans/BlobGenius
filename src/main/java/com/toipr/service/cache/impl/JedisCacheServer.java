package com.toipr.service.cache.impl;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.node.CacheNode;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.JedisPoolServices;
import com.toipr.util.bean.BeanFactory;
import com.toipr.util.bean.Beans;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.*;

/**
 * Redis缓存管理器
 */
public class JedisCacheServer implements CacheServer {
    protected CacheNode config;
    protected JedisPool jedisPool;

    protected Map<Class<?>, BeanFactory> mapTarget = new HashMap<Class<?>, BeanFactory>();

    public boolean init(CacheNode node){
        if(!JedisPoolServices.createInstance(node.getHost(), node.getDbPass())){
            return false;
        }
        jedisPool = JedisPoolServices.getJedisPool(node.getHost());
        if(jedisPool==null){
            return false;
        }
        this.config = node;
        return true;
    }

    /**
     * 设置下一个处理的数据服务器，责任链设计模式
     * 解决数据多节点复制 与 失败多点重试问题
     * @param type -1=前一个 1=后一个 0=主服务器
     * @param server 数据服务器
     */
    public void setServer(int type, CacheServer server){

    }

    /**
     * 向频道channel发布消息message
     * @param channel 频道名称
     * @param message 消息内容
     * @return 成功=true 失败=false
     */
    public boolean publish(String channel, String message){
        Jedis client = getClient();
        if(client==null){
            return false;
        }

        long ret = client.publish(channel, message);
        client.close();
        return (ret>0);
    }

    /**
     * 判断缓存是否存在
     * @param key 缓存名称
     * @return true=存在
     */
    public boolean exists(String key){
        Jedis client = getClient();
        if(client==null){
            return false;
        }

        boolean ret = client.exists(key);
        client.close();
        return ret;
    }
    public long exists(String... keys){
        Jedis client = getClient();
        if(client==null){
            return 0;
        }

        long ret = client.exists(keys);
        client.close();
        return ret;
    }

    /**
     * 设置缓存失效时间
     * @param key 缓存名称
     * @param seconds 失效时间 单位秒s
     */
    public void expire(String key, int seconds){
        Jedis client = getClient();
        if(client==null){
            return;
        }
        client.expire(key, seconds);
        client.close();
    }

    /**
     * 添加缓存对象
     * @param key 缓存名称
     * @param expire 失效时间 单位ms
     * @param flags 缓存属性
     * @param value 缓存值
     * @return 成功=true 失败=false
     */
    public boolean addCache(String key, int expire, int flags, Object value){
        Jedis client = getClient();
        if(client==null){
            return false;
        }

        SetParams params = getSetParams(expire, flags);
        String ret = client.set(key, value.toString(), params);
        client.close();
        return isDone(ret);
    }

    /**
     * 添加缓存对象，从target对象获取name属性
     * @param key 缓存名称
     * @param expire 失效时间 单位ms
     * @param flags 缓存属性
     * @param name 对象属性
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    public boolean addCache(String key, int expire, int flags, String name, Object target){
        Object objValue = getObjectValue(name, target);
        if(objValue==null){
            return false;
        }
        return addCache(key, expire, flags, objValue);
    }

    /**
     * 添加BEAN对象
     * @param key 缓存名称
     * @param expire 失效时间 单位ms
     * @param flags 缓存属性
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    public boolean addObject(String key, int expire, int flags, Object target){
        Map<String, String> props = new HashMap<String, String>();
        if(!getProperties(target, props)){
            return false;
        }

        Jedis client = getClient();
        if(client==null){
            return false;
        }

        String ret = client.hmset(key, props);
        if(expire>0 && isDone(ret)){
            client.expire(key, expire);
        }
        client.close();
        return isDone(ret);
    }

    /**
     * 添加哈希缓存
     * @param map 哈希名称
     * @param flags 缓存属性
     * @param field 缓存字段名称
     * @param value 对象实例
     * @return 成功=true 失败=false
     */
    public boolean addMapCache(String map, int flags, String field, Object value){
        Jedis client = getClient();
        if(client==null){
            return false;
        }

        long ret = 0;
        if((flags& CacheServer.ifNotExists)==0) {
            ret = client.hset(map, field, value.toString());
        } else {
            ret = client.hsetnx(map, field, value.toString());
        }
        client.close();
        return (ret>0);
    }

    /**
     * 添加哈希缓存，从target对象获取name属性
     * @param map 哈希名称
     * @param flags 缓存属性
     * @param field 缓存字段名称
     * @param name 对象属性名称
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    public boolean addMapCache(String map, int flags, String field, String name, Object target){
        Object objValue = getObjectValue(name, target);
        if(objValue==null){
            return false;
        }
        return addMapCache(map, flags, field, objValue);
    }

    /**
     * 添加集合缓存
     * @param set 集合名称
     * @param score 排序值，-1=无序集合
     * @param value 属性值
     * @return 成功=true 失败=false
     */
    public boolean addSetCache(String set, double score, Object value){
        Jedis client = getClient();
        if(client==null){
            return false;
        }

        long ret = 0;
        if(score==-1) {
            ret = client.sadd(set, value.toString());
        } else {
            ret = client.zadd(set, score, value.toString());
        }
        client.close();
        return (ret>0);
    }

    /**
     * 添加集合缓存，从target对象获取name属性
     * @param set 集合名称
     * @param score 排序值，-1=无序集合
     * @param name 属性值
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    public boolean addSetCache(String set, double score, String name, Object target){
        Object objValue = getObjectValue(name, target);
        if(objValue==null){
            return false;
        }
        return addSetCache(set, score, objValue);
    }

    /**
     * 添加列表缓存
     * @param list 列表名称
     * @param value 属性值
     * @param isHead true=添加到头部 false=添加到尾部
     * @return 成功=true 失败=false
     */
    public boolean addListCache(String list, boolean isHead, Object value){
        Jedis client = getClient();
        if(client==null){
            return false;
        }

        long ret = 0;
        if(isHead){
            ret = client.lpush(list, value.toString());
        } else {
            ret = client.rpush(list, value.toString());
        }
        client.close();
        return (ret>0);
    }

    /**
     * 添加列表缓存
     * @param list 缓存名称
     * @param name 属性名称
     * @param target 对象实例
     * @param isHead true=添加到头部 false=添加到尾部
     * @return 成功=true 失败=false
     */
    public boolean addListCache(String list, boolean isHead, String name, Object target){
        Object objValue = getObjectValue(name, target);
        if(objValue==null){
            return false;
        }
        return addListCache(list, isHead, objValue);
    }

    /**
     * 删除缓存
     * @param name 缓存名称
     */
    public void removeCache(String name){
        Jedis client = getClient();
        if(client==null){
            return;
        }

        client.del(name);
        client.close();
    }

    /**
     * 删除哈希缓存
     * @param name 缓存名称
     * @param key 哈希主键
     */
    public void removeCache(String name, String key){
        Jedis client = getClient();
        if(client==null){
            return;
        }

        client.hdel(name, key);
        client.close();
    }

    /**
     * 获取字符串缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    public String getString(String name, String defValue){
        Jedis client = getClient();
        if(client==null){
            return null;
        }

        String ret = client.get(name);
        client.close();
        if(ret==null){
            return defValue;
        }
        return ret;
    }
    public String getString(String name, String key, String defValue){
        Jedis client = getClient();
        if(client==null){
            return null;
        }

        String ret = client.hget(name, key);
        client.close();
        if(ret==null){
            return defValue;
        }
        return ret;
    }

    /**
     * 获取布尔缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    public boolean getBoolean(String name, boolean defValue){
        String ret = getString(name, null);
        if(ret==null){
            return defValue;
        }
        return Boolean.parseBoolean(ret);
    }

    public boolean getBoolean(String name, String key, boolean defValue){
        String ret = getString(name, key, null);
        if(ret==null){
            return defValue;
        }
        return Boolean.parseBoolean(ret);
    }

    /**
     * 获取整数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    public int getInteger(String name, int defValue){
        String ret = getString(name, null);
        if(ret==null){
            return defValue;
        }
        return Integer.parseInt(ret);
    }
    public int getInteger(String name, String key, int defValue){
        String ret = getString(name, key, null);
        if(ret==null){
            return defValue;
        }
        return Integer.parseInt(ret);
    }

    /**
     * 获取大整数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    public long getLong(String name, long defValue){
        String ret = getString(name, null);
        if(ret==null){
            return defValue;
        }
        return Long.parseLong(ret);
    }
    public long getLong(String name, String key, long defValue){
        String ret = getString(name, key, null);
        if(ret==null){
            return defValue;
        }
        return Long.parseLong(ret);
    }

    /**
     * 获取浮点数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    public float getFloat(String name, float defValue){
        String ret = getString(name, null);
        if(ret==null){
            return defValue;
        }
        return Float.parseFloat(ret);
    }
    public float getFloat(String name, String key, float defValue){
        String ret = getString(name, key, null);
        if(ret==null){
            return defValue;
        }
        return Float.parseFloat(ret);
    }

    /**
     * 获取双精度浮点数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    public double getDouble(String name, double defValue){
        String ret = getString(name, null);
        if(ret==null){
            return defValue;
        }
        return Double.parseDouble(ret);
    }
    public double getDouble(String name, String key, double defValue){
        String ret = getString(name, key, null);
        if(ret==null){
            return defValue;
        }
        return Double.parseDouble(ret);
    }

    /**
     * 获取JSON对象，要求存储的值符合JSON规范
     * @param name 缓存名称
     * @return JSON对象
     */
    public Object getJsonObject(String name){
        String ret = getString(name, null);
        if(ret==null){
            return null;
        }
        return JSONObject.parse(ret);
    }
    public Object getJsonObject(String name, String key){
        String ret = getString(name, key, null);
        if(ret==null){
            return null;
        }
        return JSONObject.parse(ret);
    }

    /**
     * 获取缓存值，并初始化一个BEAN对象
     * @param name 缓存名称
     * @param clazz 对象类型
     * @return 失败=null
     */
    public Object getObject(String name, Class<?> clazz){
        Map<String, String> props = getMap(name);
        if(props==null){
            return null;
        }
        return createObject(clazz, props);
    }

    /**
     * 获取集合缓存
     * @param name 缓存名称
     * @return
     */
    public Set<String> getSet(String name){
        Jedis client = getClient();
        if(client==null){
            return null;
        }

        Set<String> ret = client.smembers(name);
        client.close();
        return ret;
    }

    /**
     * 获取列表缓存
     * @param name 缓存名称
     * @return
     */
    public List<String> getList(String name){
        Jedis client = getClient();
        if(client==null){
            return null;
        }

        long ret = client.llen(name);
        if(ret==0){
            client.close();
            return null;
        }

        List<String> plist = client.lrange(name, 0, ret-1);
        client.close();
        return plist;
    }

    /**
     * 获取哈希缓存
     * @param name 缓存名称
     * @return
     */
    public Map<String, String> getMap(String name){
        Jedis client = getClient();
        if(client==null){
            return null;
        }

        Map<String, String> ret = client.hgetAll(name);
        client.close();
        return ret;
    }

    protected synchronized Jedis getClient(){
        Jedis client = jedisPool.getResource();
        if(client==null){
            return null;
        }

        int dbIndex = config.getDbIndex();
        if(dbIndex>=0){
            client.select(dbIndex);
        }
        return client;
    }

    protected SetParams getSetParams(int expire, int flags){
        SetParams params = new SetParams();
        if(expire>0) {
            params.ex(expire);
        }
        if((flags& CacheServer.ifNotExists)!=0){
            params.nx();
        }
        if((flags& CacheServer.onlyExists)!=0){
            params.xx();
        }
        return params;
    }

    protected boolean isDone(String ret){
        if(ret==null){
            return false;
        }
        if(ret.indexOf("ERR")>=0){
            return false;
        }
        return true;
    }

    protected Object getObjectValue(String name, Object target){
        BeanFactory bean = null;
        Class<?> clazz = target.getClass();
        if(mapTarget.containsKey(clazz)){
            bean = mapTarget.get(clazz);
        } else {
            bean = Beans.newBeanFactory(target.getClass());
            mapTarget.put(clazz, bean);
        }
        return bean.getProperty(target, name);
    }

    protected boolean getProperties(Object target, Map<String, String> props){
        BeanFactory bean = null;
        Class<?> clazz = target.getClass();
        if(mapTarget.containsKey(clazz)){
            bean = mapTarget.get(clazz);
        } else {
            bean = Beans.newBeanFactory(clazz);
            mapTarget.put(clazz, bean);
        }
        return bean.getProperties2(target, props);
    }

    protected Object createObject(Class<?> clazz, Map<String, String> props){
        BeanFactory bean = null;
        if(mapTarget.containsKey(clazz)){
            bean = mapTarget.get(clazz);
        } else {
            bean = Beans.newBeanFactory(clazz);
            mapTarget.put(clazz, bean);
        }

        Object target = bean.createObject(null);
        if(target==null){
            return null;
        }
        for(Map.Entry<String, String> item : props.entrySet()){
            bean.setProperty2(target, item.getKey(), item.getValue());
        }
        return target;
    }
}
