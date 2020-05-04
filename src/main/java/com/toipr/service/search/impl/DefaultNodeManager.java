package com.toipr.service.search.impl;

import com.toipr.mapper.node.NameNodeMapper;
import com.toipr.model.node.NameNode;
import com.toipr.service.DefaultService;
import com.toipr.service.search.NodeManager;
import com.toipr.service.search.ObjectIndexer;
import com.toipr.service.search.ObjectSearcher;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultNodeManager extends DefaultService implements NodeManager {
    protected Map<String, ObjectIndexer> mapIndexer = new HashMap<String, ObjectIndexer>();
    protected Map<String, ObjectSearcher> mapSearcher = new HashMap<String, ObjectSearcher>();

    public DefaultNodeManager(ApplicationContext context){
        super(context);
    }

    @Override
    public boolean init(String[] mappers) {
        if(!super.init(mappers)){
            return false;
        }

        try(SqlSession session = mybatis.getSession()){
            NameNodeMapper mapper = session.getMapper(NameNodeMapper.class);
            List<NameNode> rlist = mapper.getAll();
            if(rlist==null || rlist.size()==0){
                return false;
            }

            String resid;
            List<NameNode> tlist = null;
            Map<String, List<NameNode>> mapNode = new HashMap<String, List<NameNode>>();
            for(NameNode node : rlist){
                resid = node.getResid();
                if(mapNode.containsKey(resid)){
                    tlist = mapNode.get(resid);
                } else {
                    tlist = new ArrayList<NameNode>();
                    mapNode.put(resid, tlist);
                }
                tlist.add(node);
            }

            for(Map.Entry<String, List<NameNode>> item : mapNode.entrySet()){
                resid = item.getKey();
                tlist = item.getValue();

                String collection = "";
                String[] hosts = new String[tlist.size()];
                for(int i=0; i<hosts.length; i++){
                    hosts[i] = tlist.get(i).getHost();
                    collection = tlist.get(i).getDbName();
                }
                if(!createIndexer(resid, collection, hosts)){
                    return false;
                }
                if(!createSearcher(resid, collection, hosts)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据资源获取对象索引器
     * @param resid 资源ID
     * @return 对象索引器
     */
    public ObjectIndexer getIndexer(String resid){
        if(mapIndexer.containsKey(resid)){
            return mapIndexer.get(resid);
        }
        return null;
    }

    /**
     * 根据资源获取对象搜索器
     * @param resid 资源ID
     * @return 对象搜索器
     */
    public ObjectSearcher getSearcher(String resid){
        if(mapSearcher.containsKey(resid)){
            return mapSearcher.get(resid);
        }
        return null;
    }

    protected boolean createIndexer(String resid, String collection, String[] hosts, Object... args){
        if(mapIndexer.containsKey(resid)){
            return true;
        }

        DataObjectIndexer indexer = new DataObjectIndexer(collection);
        if(!indexer.init(hosts, args)){
            return false;
        }
        mapIndexer.put(resid, indexer);
        return true;
    }

    protected boolean createSearcher(String resid, String collection, String[] hosts, Object... args){
        if(mapSearcher.containsKey(resid)){
            return true;
        }

        ObjectSearcher myobj = new DataObjectSearcher(collection);
        if(!myobj.init(hosts, args)){
            return false;
        }
        mapSearcher.put(resid, myobj);
        return true;
    }
}
