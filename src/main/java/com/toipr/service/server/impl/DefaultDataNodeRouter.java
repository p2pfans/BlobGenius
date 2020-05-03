package com.toipr.service.server.impl;

import com.toipr.conf.MySettings;
import com.toipr.model.data.DataResource;
import com.toipr.model.data.DataRule;
import com.toipr.model.node.DataNode;
import com.toipr.service.DefaultService;
import com.toipr.service.data.BlobStores;
import com.toipr.service.node.NodeService;
import com.toipr.service.node.NodeServices;
import com.toipr.service.resource.ResourceService;
import com.toipr.service.resource.ResourceServices;
import com.toipr.service.resource.RuleService;
import com.toipr.service.rule.RuleRouter;
import com.toipr.service.rule.RuleRouters;
import com.toipr.service.server.*;
import com.toipr.socket.SocketServices;
import com.toipr.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDataNodeRouter extends DefaultService implements DataNodeRouter {
    protected String[] mappers;
    protected DataNodeRouter router;

    protected List<DataNode> lstNodes = new ArrayList<DataNode>();
    protected List<DataRule> lstRules = new ArrayList<DataRule>();

    protected Map<String, Object> mapRouters = new HashMap<String, Object>();

    protected Map<String, Object> mapResTypes = new HashMap<String, Object>();

    public DefaultDataNodeRouter(ApplicationContext context){
        super(context);
    }

    @Override
    public boolean init(String[] mappers){
        this.mappers = mappers;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", "default");

        ResourceService service = ResourceServices.getInstance();

        List<DataResource> rlist = new ArrayList<DataResource>();
        int total = service.getResourceList(params, 0, -1, rlist);
        if(total<=0){
            return false;
        }
        return addResource(rlist.get(0));
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
        return true;
    }

    /**
     * 删除数据存储结点
     * @param hid 节点ID
     * @return 成功=true 失败=false
     */
    public boolean removeServer(String hid){
        return true;
    }

    /**
     * 添加数据存储节点
     * @param node 数据节点
     * @return 成功=true 失败=false
     */
    public boolean addServer(DataNode node){
        return true;
    }

    /**
     * 添加数据资源
     * @param res 数据资源对象
     * @return 成功=true 失败=false
     */
    public boolean addResource(DataResource res){
        /**
         * 查询资源对应映射规则
         */
        RuleService ruleService = ResourceServices.getRuleService();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("rid", res.getRid());
        List<DataRule> lstRule = new ArrayList<DataRule>();
        int total = ruleService.getRuleList(params, 0, -1, lstRule);
        if(total==0){
            return false;
        }
        params.clear();

        /**
         * 查询资源数据存储节点
         */
        NodeService service = NodeServices.getInstance();
        List<DataNode> lstNode = new ArrayList<DataNode>();
        params.put("rid", res.getRid());
        total = service.getNodeList(params, 0, 0, lstNode);
        if(total==0){
            return false;
        }
        params.clear();

        /**
         * 保存数据节点与资源存储映射规则
         */
        lstNodes.addAll(lstNode);
        lstRules.addAll(lstRule);

        for(DataNode node : lstNode){
            if(!Utils.isNullOrEmpty(node.getFilePath())){
                /**
                 * 数据块存储在节点的文件系统，初始化TCP通信
                 */
                String key = node.getRid() + "_" + node.getHid();
                if (!BlobStores.createBlobStore(node.getRid(), key, false, node)) {
                    return false;
                }
            }
        }

        /**
         * 资源ID + 数据类型分组
         */
        String resType;
        for(int i=0; i<lstRule.size(); i++){
            DataRule rule = lstRule.get(i);
            resType = rule.getRid() + "_" + rule.getDataType();
            if(!mapResTypes.containsKey(resType)){
                mapResTypes.put(resType, rule);
            }
        }
        return true;
    }

    /**
     * 获取服务器路由主键
     * @param rid 资源代码
     * @param type 数据类型 objects=DataObject, blobIds=DataBlobIds, blobs=DataBlob
     * @param doid 数据对象ID
     * @param oid 拥有者ID
     * @param isTable true=数据表路由 false=数据节点路由
     * @param userData 自定义参数
     * @return 路由主键
     */
    public String getRouterKey(String rid, String type, String doid, String oid, boolean isTable, Object userData){
        RuleRouter ruleRouter = RuleRouters.getRuleRouter(rid, type);
        if(ruleRouter==null){
            return null;
        }
        if(isTable){
            return ruleRouter.getTableKey(doid, oid, userData);
        }
        return ruleRouter.getNodeKey(doid, oid, userData);
    }

    /**
     * 判断distKey主键是否属于自己调度范围
     * @param distKey 节点路由主键
     * @return 成功=true 失败=false
     */
    public boolean inThisRouter(String distKey){
        return false;
    }

    /**
     * 根据数据类型与ID分配数据节点
     * @param rid 资源ID
     * @param type 数据类型 object=DataObject, ids=DataBlobIds, blob=DataBlob
     * @param doid 数据对象ID
     * @param oid 拥有者ID
     * @param isUpdate 是否是更新操作
     * @param userData 自定义调度参数
     * @return 数据服务器
     */
    public DataServer getServer(String rid, String type, String doid, String oid, boolean isUpdate, Object userData){
        String resType = rid + "_" + type;
        if(!mapResTypes.containsKey(resType)){
            return null;
        }

        DataServer server = null;
        if(router!=null){
            /**
             * 有外部设置路由器时，优先委派外部路由器定位数据存储结点
             * 外部定位失败时，再由自己查找节点，类似ClassLoader的双亲委托机制
             */
            server = router.getServer(rid, type, doid, oid, isUpdate, userData);
        }

        if(server==null){
            /**
             * 存储节点按资源rid + 数据类型dataType分组
             */
            DataNodeRouter group = getRouter(rid, type);
            if(group==null){
                return null;
            }
            server = group.getServer(rid, type, doid, oid, isUpdate, userData);
        }
        return server;
    }

    /**
     * 根据数据类型与ID分配数据节点
     * @param rid 资源代码
     * @param type 数据类型 objects=DataObject, blobIds=DataBlobIds, blobs=DataBlob
     * @param distKey 映射主键
     * @param isUpdate 是否是更新操作
     * @param userData 自定义调度参数
     * @return 数据服务器
     */
    public DataServer getServer(String rid, String type, String distKey, boolean isUpdate, Object userData){
        DataNodeRouter nodeRouter = getRouter(rid, type);
        if(nodeRouter==null){
            return null;
        }
        return nodeRouter.getServer(rid, type, distKey, isUpdate, userData);
    }

    /**
     * 根据资源名称与数据类型获取节点路由器
     * @param rid 资源ID
     * @param dataType 数据类型
     * @return 存储结点路由器
     */
    protected synchronized  DataNodeRouter getRouter(String rid, String dataType){
        /**
         * 优先查找哈希表，是否已存在资源调度分组
         */
        String resType = rid + "_" + dataType;
        if(mapRouters.containsKey(rid)){
            return (DataNodeRouter)mapRouters.get(resType);
        }

        DefaultDataServerDispatcher group = new DefaultDataServerDispatcher(rid, dataType, this.mappers);
        /**
         * 按资源+数据类型，设置资源映射规则
         */
        for(int t=0; t<lstRules.size(); t++){
            DataRule rule = lstRules.get(t);
            if(rid.compareTo(rule.getRid())==0 && dataType.compareTo(rule.getDataType())==0){
                group.addRule(rule);
            }
        }

        /**
         * 按资源+数据类型，添加数据存储结点
         */
        for(int i=0; i<lstNodes.size(); i++){
            DataNode node = lstNodes.get(i);
            if(rid.compareTo(node.getRid())==0 && dataType.compareTo(node.getDataType())==0){
                group.addServer(node);
            }
        }
        mapRouters.put(resType, group);
        return group;
    }
}
