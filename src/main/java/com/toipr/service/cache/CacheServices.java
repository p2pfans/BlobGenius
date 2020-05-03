package com.toipr.service.cache;

import com.toipr.service.cache.impl.DefaultCacheRouter;
import com.toipr.service.cache.impl.DefaultCacheSubscriber;
import org.springframework.context.ApplicationContext;

public class CacheServices {
    protected static CacheRouter instance = null;
    protected static CacheSubscriber subscriber = null;

    /**
     * 创建缓存路由对象
     * @param context 应用程序上下文
     * @param mappers 映射XML文件加载路径
     * @return 成功=true 失败=false
     */
    public static synchronized boolean createInstance(ApplicationContext context, String[] mappers){
        if(instance==null){
            DefaultCacheRouter myobj = new DefaultCacheRouter(context);
            if(!myobj.init(mappers)){
                return false;
            }
            instance = myobj;
        }
        return true;
    }

    public static CacheRouter getInstance(){
        return instance;
    }

    /**
     * 根据数据库名、缓存主键与自定义参数获取缓存服务器
     * @param dbname 数据库名称
     * @param key 缓存主键
     * @param isUpdate true=更新操作
     * @param userData 自定义对象
     * @return 缓存服务器
     */
    public static CacheServer getServer(String dbname, String key, boolean isUpdate, Object userData){
        return instance.getServer(dbname, key, isUpdate, userData);
    }

    public static synchronized boolean createSubscriber(String host, int dbIndex){
        if(subscriber==null){
            DefaultCacheSubscriber myobj = new DefaultCacheSubscriber(host, dbIndex);
            subscriber = myobj;
        }
        return true;
    }

    public static CacheSubscriber getSubscriber(String host){
        return subscriber;
    }
}
