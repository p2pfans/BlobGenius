package com.toipr.service.cache.impl;

import com.toipr.model.node.CacheNode;
import com.toipr.service.cache.CacheRouter;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.rule.RuleRouter;
import com.toipr.service.rule.RuleRouters;
import com.toipr.service.server.DataServer;
import com.toipr.util.Utils;

import java.util.*;

public class DefaultCacheServerDispatcher implements CacheRouter {
    protected class DistKeyEntry{
        /**
         * 格式：范围型[beg-end] 如00-4f
         * 格式：离散型[v1,v2,v3] 如00,10,20,30
         */
        protected String distKey;
        /**
         * 开始范围
         */
        protected String begKey;
        /**
         * 结束范围
         */
        protected String endKey;

        /**
         * 是否存储所有对象
         */
        protected boolean isAll;

        /**
         * 是否为范围匹配
         */
        protected boolean isRange;
        /**
         * 用半角逗号','隔开的多值启用
         */
        protected Set<String> keySet = null;

        public DistKeyEntry(String distKey){
            this.distKey = distKey;
            if(Utils.isNullOrEmpty(distKey) || distKey.compareToIgnoreCase("all")==0){
                isAll = true;
                return;
            }

            int pos = distKey.indexOf("-");
            if(pos>0){
                begKey = distKey.substring(0, pos).trim();
                endKey = distKey.substring(pos+1).trim();
                begKey = begKey.toLowerCase();
                endKey = endKey.toLowerCase();
                isRange = true;
            } else {
                isRange = false;
                String[] sArr = distKey.split(",");
                if(sArr.length==1){
                    begKey = sArr[0].toLowerCase();
                } else {
                    keySet = new HashSet<String>();
                    for (int i = 0; i < sArr.length; i++) {
                        String line = sArr[i].trim();
                        if (line.length() > 0) {
                            keySet.add(sArr[i].toLowerCase());
                        }
                    }
                }
            }
        }

        /**
         * 判断映射主键是否属于管辖范围
         * @param key 映射主键
         * @return
         */
        public boolean inThisNode(String key){
            if(isAll){
                return true;
            }
            if(isRange){
                if(key.compareTo(begKey)<0){
                    return false;
                }
                if(!Utils.isNullOrEmpty(endKey)){
                    if(key.compareTo(endKey)>0){
                        return false;
                    }
                }
                return true;
            }

            if(keySet!=null){
                return keySet.contains(key);
            }
            return (key.compareTo(begKey)==0);
        }
    }

    protected class CacheNodeEntry{
        protected CacheNode node;
        public CacheNode getNode(){
            return this.node;
        }

        protected CacheServer server;
        public CacheServer getServer(){
            if(server==null){
                synchronized (this){
                    if(server==null){
                        JedisCacheServer myobj = new JedisCacheServer();
                        if(!myobj.init(node)){
                            return null;
                        }
                        server = myobj;
                    }
                }
            }
            return server;
        }

        protected DistKeyEntry distKey;
        protected List<DistKeyEntry> lstKeys;

        public CacheNodeEntry(CacheNode node){
            this.node = node;

            DistKeyEntry item = null;
            String distKey = node.getDistKey();
            String[] sArr = distKey.split(";;");
            if(sArr.length==1){
                this.distKey = new DistKeyEntry(sArr[0].trim());
            } else {
                lstKeys = new ArrayList<DistKeyEntry>();
                for (int i = 0; i < sArr.length; i++) {
                    item = new DistKeyEntry(sArr[i].trim());
                    lstKeys.add(item);
                }
            }
        }

        /**
         * 是否主服务器
         * @return true=主服务器
         */
        public boolean isMaster(){
            return node.getMaster();
        }

        /**
         * 判断数据对象是否存储在本节点
         * @param key 数据对象映射主键
         * @return true=存储在本节点
         */
        public boolean inThisNode(String key){
            if(distKey!=null){
                return distKey.inThisNode(key);
            }

            for(int i=0; i<lstKeys.size(); i++){
                DistKeyEntry temp = lstKeys.get(i);
                if(temp.inThisNode(key)){
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 数据节点组，存储相同映射主键distKey(如a3)的数据对象
     */
    protected class CacheServerGroup{
        protected String distKey;
        public String getDistKey(){
            return this.distKey;
        }

        protected CacheNodeEntry primary = null;
        protected List<CacheNodeEntry> lstServers = new LinkedList<CacheNodeEntry>();

        public CacheServerGroup(String distKey){
            this.distKey = distKey;
        }

        /**
         * 添加数据存储结点
         * @param item
         */
        public synchronized void addNode(CacheNodeEntry item){
            CacheNode node = item.getNode();
            /**
             * 只有一个master起作用，就是最后一个
             */
            if(!node.getMaster()){
                lstServers.add(item);
            } else {
                if(primary!=null){
                    lstServers.add(primary);
                }
                primary = item;
            }

            /**
             * 主/从配置模式，清除责任链，写主/读随机
             */
            initDataChain(primary!=null);
        }

        /**
         * 初始化数据处理链
         * @param breakChain true=清除责任链 false=构建责任链
         */
        protected void initDataChain(boolean breakChain){
            CacheServer priServer = null;
            if(primary!=null){
                priServer = primary.getServer();
                priServer.setServer(DataServer.nextNode, null);
                priServer.setServer(DataServer.prevChain, null);
                priServer.setServer(DataServer.primary, priServer);
            }

            /**
             * 责任链设计模式
             * 解决问题1：解决同组内数据多处同时修改问题
             * 解决问题2：解决同组内节点失效向后传导问题
             * 解决问题3：主服务器解决读失败备份支援问题，解决数据同步时差问题
             * 方法：构建一个双向无循环链表,每个节点设置主服务器
             */
            CacheServer head = null, prev = null, server = null;
            for (CacheNodeEntry node : lstServers) {
                server  = node.getServer();
                if(breakChain){
                    server.setServer(DataServer.nextNode, null);
                    server.setServer(DataServer.prevNode, null);
                } else {
                    if (prev != null) {
                        prev.setServer(DataServer.nextNode, server);
                        server.setServer(DataServer.prevNode, prev);
                        prev = server;
                    } else {
                        prev = server;
                        head = server;
                    }
                }
                server.setServer(DataServer.primary, priServer);
            }
            if(head!=null){
                head.setServer(DataServer.prevNode, null);
            }
            if(server!=null) {
                server.setServer(DataServer.nextNode, null);
            }
            if(priServer!=null){
                /**
                 * 设置主服务器后续节点
                 * 解决主服务器写失败的责任转移问题
                 */
                priServer.setServer(DataServer.nextNode, head);
            }
        }

        /**
         * 故障/维护等问题，节点退出后，需要删除节点
         * @param item 节点对象
         */
        public synchronized void removeNode(CacheNodeEntry item){
            if(item==primary){
                primary = null;
                for (CacheNodeEntry node : lstServers) {
                    if(node.getNode().getMaster()){
                        primary = node;
                        break;
                    }
                }
            } else {
                lstServers.remove(item);
            }
            initDataChain(primary!=null);
        }

        /**
         * 获取数据服务器对象
         * @param isUpdate true=更新操作 false=读取操作
         * @return 数据服务器
         */
        public synchronized CacheServer getServer(boolean isUpdate){
            CacheServer server = null;
            /**
             * 写操作，请求主服务器
             */
            if(isUpdate && primary!=null){
                server = primary.getServer();
            }

            if(server==null && lstServers.size()>0){
                if(lstServers.size()==1){
                    server = lstServers.get(0).getServer();
                } else {
                    int count = 0;
                    while(count<lstServers.size()){
                        CacheNodeEntry node = lstServers.get(0);
                        lstServers.remove(0);
                        lstServers.add(node);

                        server = node.getServer();
                        if(server!=null){
                            break;
                        }
                        count++;
                    }
                }
            }
            return server;
        }
    }

    protected String dbname;
    public String getDbname(){
        return this.dbname;
    }

    protected List<CacheNodeEntry> lstNodes = new ArrayList<CacheNodeEntry>();
    protected Map<String, CacheServerGroup> mapGroup = new HashMap<String, CacheServerGroup>();

    public DefaultCacheServerDispatcher(String dbname){
        this.dbname = dbname;
    }

    /**
     * 添加缓存节点
     * @param node 节点对象
     * @return 成功=true 失败=false
     */
    public boolean addNode(CacheNode node){
        if(findNode(node.getHost(), node.getName())!=null){
            return true;
        }

        CacheNodeEntry item = new CacheNodeEntry(node);
        lstNodes.add(item);
        return true;
    }

    /**
     * 判断映射主键是否在本节点缓存
     * @param distKey 映射主键
     * @return true=是 false=否
     */
    public boolean inThisNode(String distKey){
        if(mapGroup.containsKey(distKey)){
            return true;
        }
        return getNodeGroup(distKey)!=null;
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
        CacheServerGroup group = getNodeGroup(distKey);
        if(group==null){
            return null;
        }
        return group.getServer(isUpdate);
    }

    /**
     * 根据数据库名、缓存主键与自定义参数获取缓存服务器
     * @param dbname 数据库名称
     * @param key 缓存主键
     * @param isUpdate true=更新操作
     * @param userData 自定义对象
     * @return 缓存服务器
     */
    public CacheServer getServer(String dbname, String key, boolean isUpdate, Object userData) {
        String distKey = getRouterKey(dbname, key, userData);
        if(distKey==null){
            return null;
        }
        return getServer(distKey, isUpdate, userData);
    }

    /**
     * 根据映射主键获取缓存服务器组
     * @param distKey 映射主键
     * @return
     */
    protected synchronized CacheServerGroup getNodeGroup(String distKey){
        CacheServerGroup group = null;
        if(mapGroup.containsKey(distKey)){
            return mapGroup.get(distKey);
        }

        boolean hasNode = false;
        group = new CacheServerGroup(distKey);
        for(int i=0; i<lstNodes.size(); i++){
            CacheNodeEntry temp = lstNodes.get(i);
            if(temp.inThisNode(distKey)){
                group.addNode(temp);
                hasNode = true;
            }
        }
        if(!hasNode){
            return null;
        }
        mapGroup.put(distKey, group);
        return group;
    }

    /**
     * 根据节点ID查找结点对象
     * @param host 节点ID
     * @param dbname 节点ID
     * @return 缓存节点对象
     */
    protected CacheNode findNode(String host, String dbname){
        for(int i=0; i<lstNodes.size(); i++){
            CacheNode temp = lstNodes.get(i).getNode();
            if(temp.getHost().compareTo(host)==0){
                return temp;
            }
        }
        return null;
    }
}
