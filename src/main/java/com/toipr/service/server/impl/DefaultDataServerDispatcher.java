package com.toipr.service.server.impl;

import com.toipr.model.data.DataResource;
import com.toipr.model.data.DataRule;
import com.toipr.model.node.DataNode;
import com.toipr.service.rule.RuleRouter;
import com.toipr.service.rule.RuleRouters;
import com.toipr.service.server.*;
import com.toipr.util.Utils;

import java.util.*;

/**
 * 数据存储节点调度
 * 负责资源resource + 数据类型dataType的存储节点路由
 * 调度规则：根据DataNode的distKey映射规则路由
 */
public class DefaultDataServerDispatcher implements DataNodeRouter {
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

    protected class DataNodeEntry{
        protected DataNode node;
        public DataNode getNode(){
            return this.node;
        }

        protected DataRule rule;
        protected String[] mappers;

        protected DataServer server;
        public DataServer getServer(){
            if(server==null){
                synchronized (this){
                    if(server==null){
                        DefaultDataServer myobj = new DefaultDataServer(node, rule);
                        if(!myobj.init(this.mappers)){
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

        public DataNodeEntry(DataNode node, DataRule rule, String[] mappers){
            this.node = node;
            this.rule = rule;
            this.mappers = mappers;

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
    protected class DataNodeGroup{
        protected String distKey;
        public String getDistKey(){
            return this.distKey;
        }

        protected DataNodeEntry primary = null;
        protected List<DataNodeEntry> lstServers = new LinkedList<DataNodeEntry>();

        public DataNodeGroup(String distKey){
            this.distKey = distKey;
        }

        /**
         * 添加数据存储结点
         * @param item
         */
        public synchronized void addNode(DataNodeEntry item){
            DataNode node = item.getNode();
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
            DataServer priServer = null;
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
            DataServer head = null, prev = null, server = null;
            for (DataNodeEntry node : lstServers) {
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
        public synchronized void removeNode(DataNodeEntry item){
            if(item==primary){
                primary = null;
                for (DataNodeEntry node : lstServers) {
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
        public synchronized DataServer getServer(boolean isUpdate){
            DataServer server = null;
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
                        DataNodeEntry node = lstServers.get(0);
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

    /**
     * 外部数据节点路由器
     */
    protected DataNodeRouter router;

    /**
     * 资源名称
     */
    protected String resource;
    /**
     * 数据类型
     */
    protected String dataType;

    /**
     * Mybatis映射文件XML加载路径
     */
    protected String[] mappers;

    protected List<DataRule> lstRules = new ArrayList<DataRule>();
    protected List<DataNodeEntry> lstNodes = new ArrayList<DataNodeEntry>();

    protected Map<String, DataNodeGroup> mapGroups = new HashMap<String, DataNodeGroup>();

    public DefaultDataServerDispatcher(String resource, String dataType, String[] mappers){
        this.resource = resource;
        this.dataType = dataType;
        this.mappers = mappers;
    }

    /**
     * 设置外部定制路由器，否则使用默认路由规则
     * @param router 数据节点路由器
     */
    public void setRouter(DataNodeRouter router){
        this.router = router;
    }

    /**
     * 添加节点路由规则
     * @param rule 路由映射规则
     * @return 成功=true 失败=false
     */
    public boolean addRule(DataRule rule){
        if(resource.compareTo(rule.getRid())!=0){
            return false;
        }
        if(findRule(rule.getRid(), rule.getDataType())!=null){
            return true;
        }

        RuleRouter router = RuleRouters.getRuleRouter(rule);
        if(router==null){
            return false;
        }
        lstRules.add(rule);
        return true;
    }

    /**
     * 删除数据存储结点
     * @param hid 节点ID
     * @return 成功=true 失败=false
     */
    public synchronized boolean removeServer(String hid){
        for(int i=0; i<lstNodes.size(); i++){
            DataNode node = lstNodes.get(i).getNode();
            if(node.getHid().compareTo(hid)==0){
                lstNodes.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 添加数据存储节点
     * @param node 数据节点
     * @return 成功=true 失败=false
     */
    public synchronized boolean addServer(DataNode node){
        if(findNode(node.getHid())!=null){
            return true;
        }

        DataRule rule = findRule(node.getRid(), node.getDataType());
        if(rule==null){
            return false;
        }
        DataNodeEntry item = new DataNodeEntry(node, rule, this.mappers);
        lstNodes.add(item);
        return true;
    }

    /**
     * 添加数据资源
     * @param res 数据资源对象
     * @return 成功=true 失败=false
     */
    public boolean addResource(DataResource res){
        return false;
    }

    /**
     * 获取服务器路由主键
     * @param rid 资源代码
     * @param type 数据类型 objects=DataObject, blobIds=DataBlobRef, blobs=DataBlob
     * @param uuid 数据对象ID
     * @param oid 拥有者ID
     * @param isTable true=数据表路由 false=数据节点路由
     * @param userData 自定义参数
     * @return 路由主键
     */
    public String getRouterKey(String rid, String type, String uuid, String oid, boolean isTable, Object userData){
        RuleRouter router = RuleRouters.getRuleRouter(rid, type);
        if(router==null){
            return null;
        }

        if(isTable){
            return router.getTableKey(uuid, oid, userData);
        }
        return router.getNodeKey(uuid, oid, userData);
    }

    /**
     * 判断distKey主键是否属于自己调度范围
     * @param distKey 节点路由主键
     * @return 成功=true 失败=false
     */
    public boolean inThisRouter(String distKey){
        if(mapGroups.containsKey(distKey)){
            return true;
        }
        return getNodeGroup(distKey)!=null;
    }

    /**
     * 根据数据类型与ID分配数据节点
     * @param rid 资源ID
     * @param type 数据类型 objects=DataObject, blobids=DataBlobRef, blobs=DataBlob
     * @param uuid 数据对象ID
     * @param oid 拥有者ID
     * @param isUpdate 是否是更新操作
     * @param userData 自定义调度参数
     * @return 数据服务器
     */
    public synchronized DataServer getServer(String rid, String type, String uuid, String oid, boolean isUpdate, Object userData){
        String distKey = getRouterKey(rid, type, uuid, oid, false, userData);
        if(distKey==null){
            return null;
        }
        if(!inThisRouter(distKey)){
            return null;
        }
        return getServer(rid, type, distKey, isUpdate, userData);
    }

    /**
     * 根据数据类型与ID分配数据节点
     * @param rid 资源代码
     * @param type 数据类型 objects=DataObject, blobIds=DataBlobRef, blobs=DataBlob
     * @param distKey 映射主键
     * @param isUpdate 是否是更新操作
     * @param userData 自定义调度参数
     * @return 数据服务器
     */
    public DataServer getServer(String rid, String type, String distKey, boolean isUpdate, Object userData){
        DataNodeGroup group = getNodeGroup(distKey);
        if(group==null){
            return null;
        }
        return group.getServer(isUpdate);
    }

    protected synchronized DataNodeGroup getNodeGroup(String distKey){
        DataNodeGroup group = null;
        if(mapGroups.containsKey(distKey)){
            return mapGroups.get(distKey);
        }

        boolean hasNode = false;
        group = new DataNodeGroup(distKey);
        for(int i=0; i<lstNodes.size(); i++){
            DataNodeEntry temp = lstNodes.get(i);
            if(temp.inThisNode(distKey)){
                group.addNode(temp);
                hasNode = true;
            }
        }
        if(!hasNode){
            return null;
        }
        mapGroups.put(distKey, group);
        return group;
    }

    /**
     * 根据节点ID查找结点对象
     * @param hid 节点ID
     * @return 数据节点对象
     */
    protected DataNode findNode(String hid){
        for(int i=0; i<lstNodes.size(); i++){
            DataNode temp = lstNodes.get(i).getNode();
            if(temp.getHid().compareTo(hid)==0){
                return temp;
            }
        }
        return null;
    }

    /**
     * 根据资源名称与数据类型查找数据映射规则
     * @param rid 资源ID
     * @param dataType 数据类型
     * @return 数据映射规则
     */
    protected DataRule findRule(String rid, String dataType){
        for(int t=0; t<lstRules.size(); t++){
            DataRule rule = lstRules.get(t);
            if(rid.compareTo(rule.getRid())==0 && dataType.compareTo(rule.getDataType())==0){
                return rule;
            }
        }
        return null;
    }
}
