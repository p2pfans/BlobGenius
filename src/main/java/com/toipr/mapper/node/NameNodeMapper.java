package com.toipr.mapper.node;

import com.toipr.model.node.NameNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NameNodeMapper {
    /**
     * 获取所有名称节点
     * @return 名称节点队列
     */
    List<NameNode> getAll();

    /**
     * 添加名称节点
     * @param item 节点实例
     * @return 成功=1 失败=0
     */
    int add(@Param("item") NameNode item);

    /**
     * 获取名称节点
     * @param hid 节点ID
     * @return 节点实例
     */
    NameNode get(@Param("hid") String hid);

    /**
     * 判断节点是否存在
     * @param host 主机地址
     * @param resid 资源ID
     * @return 存在=节点ID 不存在=null
     */
    String exists(@Param("host") String host, @Param("resid") String resid);

    /**
     * 删除名称节点
     * @param hid 节点ID
     * @return 成功=1 失败=0
     */
    int remove(@Param("hid") String hid);

    /**
     * 设置节点状态
     * @param hid 节点ID
     * @param state 工作状态
     * @return 成功=1 失败=0
     */
    int setState(@Param("hid") String hid, @Param("state") int state);
}
