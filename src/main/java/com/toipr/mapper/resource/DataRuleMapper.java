package com.toipr.mapper.resource;

import com.toipr.model.data.DataRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DataRuleMapper {
    /**
     * 添加数据节点映射规则
     * @param item 映射规则
     * @return 成功=1 失败=0
     */
    int addRule(@Param("item") DataRule item);

    /**
     * 删除数据节点映射规则
     * @param rid 资源ID
     * @return 成功=1 失败=0
     */
    int removeRule(@Param("rid") String rid);

    /**
     * 判断规则是否存在
     * @param oid 拥有者ID(Owner ID)
     * @param resource 资源名称
     * @param dataType 数据类型
     * @return 存在=记录ID 失败=null
     */
    String ruleExists(@Param("oid") String oid, @Param("resource") String resource, @Param("dataType") String dataType);

    /**
     * 设置节点映射规则
     * @param rid 资源ID
     * @param ruleHost 映射规则
     * @return 成功=1 失败=0
     */
    int setRuleHost(@Param("rid") String rid, @Param("ruleHost") String ruleHost);

    /**
     * 设置数据表映射规则
     * @param rid 资源ID
     * @param ruleTable 映射规则
     * @return 成功=1 失败=0
     */
    int setRuleTable(@Param("rid") String rid, @Param("ruleTable") String ruleTable);

    /**
     * 设置存储数据库名与数据表名
     * @param rid 资源ID
     * @param dbName 数据库名
     * @param tblName 数据表名
     * @return 成功=1 失败=0
     */
    int setTable(@Param("rid") String rid, @Param("dbName") String dbName, @Param("tblName") String tblName);

    /**
     * 设置数据库或数据表创建
     * @param rid 资源ID
     * @param isTable true=数据表 false=数据库
     * @param sqlText SQL语句
     * @return
     */
    int setTableSQL(@Param("rid") String rid, @Param("isTable") boolean isTable, @Param("sqlText") String sqlText);

    /**
     * 根据资源名称与数据类型获取节点映射规则
     * @param rid 资源ID
     * @return 映射规则对象
     */
    DataRule getRule(@Param("rid") String rid);

    /**
     * 统计节点映射规则数量
     * @param params 参数表
     * @return 节点规则数量
     */
    int count(@Param("params") Map<String, Object> params);

    /**
     * 获取数据节点映射规则列表
     * @param params 参数表
     * @param start 开始记录
     * @param count 记录数量
     * @return 映射规则列表
     */
    List<DataRule> getRuleList(@Param("params") Map<String, Object> params, @Param("start") int start, @Param("count") int count);
}
