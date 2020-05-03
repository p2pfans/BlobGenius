package com.toipr.service.node;

import com.toipr.model.node.DataNode;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NodeService {
    /**
     * 添加数据节点
     * @param node 节点对象
     * @return true=成功 false=失败
     */
    boolean addNode(DataNode node);

    /**
     * 设置主机状态
     * @param hid 主机ID
     * @param state 工作状态
     * @return true=成功 false=失败
     */
    boolean setState(String hid, int state);

    /**
     * 增加访问次数，设置最后访问时间
     * @param hid 节点ID
     * @param visit 新增访问次数
     * @param lastAccess 最近访问时间
     * @return true=成功 false=失败
     */
    boolean incVisitAndLastAccess(String hid, int visit, Date lastAccess);

    /**
     * 测试节点是否能正常链接
     * @param node 节点对象
     * @return true='success' false=错误信息
     */
    String testNode(DataNode node);

    /**
     * 测试节点是否工作正常
     * @param hid 节点ID
     * @return true='success' false=错误信息
     */
    String testNode(String hid);

    /**
     * 判断节点是否存在
     * @param protocol 传输协议
     * @param dataType 数据类型
     * @param host 主机地址与端口
     * @return 成功=HID 失败=null
     */
    String nodeExists(String protocol, String dataType, String host);

    /**
     * 删除主机节点
     * @param hid 主机ID
     * @return true=成功 false=失败
     */
    boolean removeNode(String hid);

    /**
     * 获取数据节点对象
     * @param hid 节点ID
     * @return 节点对象实例
     */
    DataNode getNode(String hid);

    /**
     * 根据资源类型、数据类型与节点状态参数获取数据节点列表
     * @param params 查询条件映射表
     * @param start 开始记录
     * @param count 记录数量
     * @param nodeList 节点列表
     * @return 符合条件的节点总数
     */
    int getNodeList(Map<String, Object> params, int start, int count, List<DataNode> nodeList);
}
