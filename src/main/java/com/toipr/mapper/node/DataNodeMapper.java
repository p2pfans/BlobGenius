package com.toipr.mapper.node;

import com.toipr.model.node.DataNode;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataNodeMapper {
    /**
     * 添加数据存储节点
     * @param node 数据节点对象
     * @return 成功=1 失败=0
     */
    int addNode(@Param("node") DataNode node);

    /**
     * 根据节点ID获取节点对象
     * @param hid 节点ID
     * @return 节点对象实例
     */
    DataNode getNode(@Param("hid") String hid);

    /**
     * 设置节点hid的状态，可以设置禁用、维护等状态
     * @param hid 节点ID
     * @param state 节点状态
     * @return 成功=1 失败=0
     */
    int setState(@Param("hid")String hid, @Param("state") int state);

    /**
     * 增加访问次数，设置最后访问时间
     * @param hid 节点ID
     * @param visit 新增访问次数
     * @param lastAccess 最近访问时间
     * @return 成功=1 失败=0
     */
    int incVisitAndLastAccess(@Param("hid")String hid, @Param("visit") int visit, @Param("lastAccess") Date lastAccess);

    /**
     * 设置节点hid的分服务器映射规则
     * @param hid 节点ID
     * @param distKey 映射规则
     * @return 成功=1 失败=0
     */
    int setDistKey(@Param("hid")String hid, @Param("distKey")String distKey);

    /**
     * 删除存储结点
     * @param hid 节点ID
     * @return 成功=1 失败=0
     */
    int removeNode(@Param("hid") String hid);

    /**
     * 查询数据节点是否存在
     * @param params 查询条件参数
     * @return 成功=节点ID 失败=null
     */
    String nodeExists(@Param("params") Map<String, Object> params);

    /**
     * 统计符合查询条件的节点数量
     * @param params 查询条件参数表
     * @return 节点数量
     */
    int count(@Param("params") Map<String, Object> params);

    /**
     * 根据查询条件获取数据节点列表
     * @param params 查询条件参数表
     * @param start 开始记录
     * @param count 记录数量
     * @return 数据节点列表
     */
    List<DataNode> getNodeList(@Param("params") Map<String, Object> params, @Param("start") int start, @Param("count") int count);
}
