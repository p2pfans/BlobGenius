package com.toipr.service.cache.impl;

import com.toipr.mapper.node.CacheNodeMapper;
import com.toipr.model.node.CacheNode;
import com.toipr.service.DefaultService;
import com.toipr.service.cache.CacheRouter;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.cache.JedisPoolServices;
import com.toipr.service.rule.RuleRouter;
import com.toipr.service.rule.RuleRouters;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCacheRouter extends DefaultService implements CacheRouter {
    protected List<CacheNode> lstNodes = new ArrayList<CacheNode>();

    protected List<CacheRouter> lstRouter = new ArrayList<CacheRouter>();
    protected Map<String, CacheRouter> mapGroup = new HashMap<String, CacheRouter>();
    protected Map<String, CacheRouter> keyRouters = new HashMap<String, CacheRouter>();

    public DefaultCacheRouter(ApplicationContext context){
        super(context);
    }

    @Override
    public boolean init(String[] mappers){
        if(!super.init(mappers)){
            return false;
        }

        try(SqlSession session = mybatis.getSession()){
            CacheNodeMapper mapper = session.getMapper(CacheNodeMapper.class);
            Map<String, Object> params = new HashMap<String, Object>();

            List<CacheNode> rlist = mapper.getNodeList(params, 0, -1);
            if(rlist==null || rlist.size()==0){
                return false;
            }
            for(CacheNode node : rlist){
                addNode(node);
            }
        }
        return true;
    }

    /**
     * 添加缓存节点
     * @param node 节点对象
     * @return 成功=true 失败=false
     */
    public synchronized boolean addNode(CacheNode node){
        if(!JedisPoolServices.createInstance(node.getHost(), node.getDbPass())){
            return false;
        }

        RuleRouter router = RuleRouters.getRuleRouter(node.getName(), node.getDistKey(), node.getDistRule());
        if(router==null){
            return false;
        }
        lstNodes.add(node);

        if(node.getName().compareTo("events")==0){
            if(!CacheServices.createSubscriber(node.getHost(), node.getDbIndex())){
                return false;
            }
        }
        return true;
    }

    /**
     * 判断映射主键是否在本节点缓存
     * @param distKey 映射主键
     * @return true=是 false=否
     */
    public boolean inThisNode(String distKey){
        if(keyRouters.containsKey(distKey)){
            return true;
        }

        for(CacheRouter router:lstRouter){
            if(router.inThisNode(distKey)){
                keyRouters.put(distKey, router);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取缓存映射主键
     * @param dbname 数据库名
     * @param key 映射主键
     * @param userData 自定义参数
     * @return 缓存映射主键
     */
    public String getRouterKey(String dbname, String key, Object userData){
        RuleRouter router = RuleRouters.getRuleRouter(dbname);
        if(router==null){
            return null;
        }
        return router.getNodeKey(key, null, userData);
    }

    /**
     * 根据映射主键获取缓存服务器
     * @param distKey 缓存主键
     * @param isUpdate true=更新操作
     * @param userData 自定义对象
     * @return 缓存服务器
     */
    public CacheServer getServer(String distKey, boolean isUpdate, Object userData){
        if(keyRouters.containsKey(distKey)){
            CacheRouter router = keyRouters.get(distKey);
            return router.getServer(distKey, isUpdate, userData);
        }

        for(CacheRouter router:lstRouter){
            if(router.inThisNode(distKey)){
                return router.getServer(distKey, isUpdate, userData);
            }
        }
        return null;
    }

    /**
     * 根据数据库名、缓存主键与自定义参数获取缓存服务器
     * @param dbname 数据库名称
     * @param key 缓存主键
     * @param isUpdate true=更新操作
     * @param userData 自定义对象
     * @return 缓存服务器
     */
    public CacheServer getServer(String dbname, String key, boolean isUpdate, Object userData){
        CacheRouter router = null;
        if(mapGroup.containsKey(dbname)){
            router = mapGroup.get(dbname);
        } else {
            DefaultCacheServerDispatcher myobj = new DefaultCacheServerDispatcher(dbname);

            boolean hasNode = false;
            for(CacheNode node : lstNodes){
                if(node.getName().compareTo(dbname)==0){
                    myobj.addNode(node);
                    hasNode = true;
                }
            }
            if(!hasNode){
                return null;
            }
            mapGroup.put(dbname, myobj);
            lstRouter.add(myobj);
            router = myobj;
        }
        return router.getServer(dbname, key, isUpdate, userData);
    }
}
