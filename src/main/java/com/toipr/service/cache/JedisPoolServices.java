package com.toipr.service.cache;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

public class JedisPoolServices {
    protected static Map<String, JedisPool> pools = new HashMap<String, JedisPool>();

    public static synchronized boolean createInstance(String host, String dbPass){
        if(pools.containsKey(host)){
            return true;
        }

        int port = 6379;
        String addr = host;
        int pos = host.indexOf(":");
        if(pos>0){
            String str = host.substring(pos+1).trim();
            addr = host.substring(0, pos).trim();
            port = Integer.parseInt(str);
            if(port<=0 || port>65535){
                return false;
            }
        }

        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setMaxIdle(10);
        conf.setMaxTotal(50);
        conf.setTestOnBorrow(false);
        conf.setTestWhileIdle(true);
        conf.setBlockWhenExhausted(true);
        conf.setMaxWaitMillis(20000);

        JedisPool myobj = new JedisPool(conf, addr, port, 20000, dbPass);
        pools.put(host, myobj);
        return true;
    }

    public static JedisPool getJedisPool(String host){
        if(pools.containsKey(host)){
            return pools.get(host);
        }
        return null;
    }
}
