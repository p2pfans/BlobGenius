package com.toipr.mapper.node;

import com.toipr.model.node.CacheNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CacheNodeMapper {
    /**
     * 添加数据存储节点
     * @param item 数据节点对象
     * @return 成功=1 失败=0
     */
    int addNode(@Param("item") CacheNode item);

    /**
     * 根据节点ID获取节点对象
     * @param id 节点ID
     * @return 节点对象实例
     */
    CacheNode getNode(@Param("id") int id);

    /**
     * 设置节点hid的状态，可以设置禁用、维护等状态
     * @param id 节点ID
     * @param state 节点状态
     * @return 成功=1 失败=0
     */
    int setState(@Param("id") int id, @Param("state") int state);

    /**
     * 设置节点存储分布主键范围
     * @param id 节点ID
     * @param distKey 映射规则
     * @return 成功=1 失败=0
     */
    int setDistKey(@Param("id") int id, @Param("distKey")String distKey);

    /**
     * 设置映射主键生成规则
     * @param id 节点ID
     * @param distKey 映射规则
     * @return 成功=1 失败=0
     */
    int setDistRule(@Param("id") int id, @Param("distKey")String distKey);

    /**
     * 删除存储结点
     * @param id 节点ID
     * @return 成功=1 失败=0
     */
    int removeNode(@Param("id") int id);

    /**
     * 查询数据节点是否存在
     * @param params 查询条件参数
     * @return 成功=节点ID 失败=null
     */
    Integer nodeExists(@Param("params") Map<String, Object> params);

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
    List<CacheNode> getNodeList(@Param("params") Map<String, Object> params, @Param("start") int start, @Param("count") int count);
}
