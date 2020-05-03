package com.toipr.service.server;

import com.toipr.model.data.DataResource;
import com.toipr.model.data.DataRule;
import com.toipr.model.node.DataNode;

/**
 * 数据节点路由器
 */
public interface DataNodeRouter {
    /**
     * 设置外部定制路由器，否则使用默认路由规则
     * @param router 数据节点路由器
     */
    void setRouter(DataNodeRouter router);

    /**
     * 添加节点路由规则
     * @param rule 路由映射规则
     * @return 成功=true 失败=false
     */
    boolean addRule(DataRule rule);

    /**
     * 添加数据资源
     * @param res 数据资源对象
     * @return 成功=true 失败=false
     */
    boolean addResource(DataResource res);

    /**
     * 添加数据存储节点
     * @param node 数据节点
     * @return 成功=true 失败=false
     */
    boolean addServer(DataNode node);

    /**
     * 删除数据存储结点
     * @param hid 节点ID
     * @return 成功=true 失败=false
     */
    boolean removeServer(String hid);

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
    String getRouterKey(String rid, String type, String doid, String oid, boolean isTable, Object userData);

    /**
     * 判断distKey主键是否属于自己调度范围
     * @param distKey 节点路由主键
     * @return 成功=true 失败=false
     */
    boolean inThisRouter(String distKey);

    /**
     * 根据数据类型与ID分配数据节点
     * @param rid 资源代码
     * @param type 数据类型 objects=DataObject, blobIds=DataBlobIds, blobs=DataBlob
     * @param distKey 映射主键
     * @param isUpdate 是否是更新操作
     * @param userData 自定义调度参数
     * @return 数据服务器
     */
    DataServer getServer(String rid, String type, String distKey, boolean isUpdate, Object userData);

    /**
     * 根据数据类型与ID分配数据节点
     * @param rid 资源代码
     * @param type 数据类型 objects=DataObject, blobIds=DataBlobIds, blobs=DataBlob
     * @param doid 数据对象ID
     * @param oid 拥有者ID
     * @param isUpdate 是否是更新操作
     * @param userData 自定义调度参数
     * @return 数据服务器
     */
    DataServer getServer(String rid, String type, String doid, String oid, boolean isUpdate, Object userData);
}
