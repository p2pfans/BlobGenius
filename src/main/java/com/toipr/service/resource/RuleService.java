package com.toipr.service.resource;

import com.toipr.model.data.DataRule;

import java.util.List;
import java.util.Map;

public interface RuleService {
    /**
     * 添加数据节点映射规则
     * @param item 映射规则
     * @return 成功=true 失败=false
     */
    boolean addRule(DataRule item);

    /**
     * 删除数据节点映射规则
     * @param rid 资源ID
     * @return 成功=true 失败=false
     */
    boolean removeRule(String rid);

    /**
     * 判断规则是否存在
     * @param oid 拥有者ID(Owner ID)
     * @param resource 资源名称
     * @param dataType 数据类型
     * @return 成功=true 失败=false
     */
    boolean ruleExists(String oid, String resource, String dataType);

    /**
     * 设置节点映射规则
     * @param rid 资源ID
     * @param ruleHost 映射规则
     * @return 成功=true 失败=false
     */
    boolean setRuleHost(String rid, String ruleHost);

    /**
     * 设置数据表映射规则
     * @param rid 资源ID
     * @param ruleTable 映射规则
     * @return 成功=true 失败=false
     */
    boolean setRuleTable(String rid, String ruleTable);

    /**
     * 设置存储数据库名与数据表名
     * @param rid 资源ID
     * @param dbName 数据库名
     * @param tblName 数据表名
     * @return 成功=true 失败=false
     */
    boolean setTable(String rid, String dbName, String tblName);

    /**
     * 设置数据库或数据表创建标签
     * @param rid 资源ID
     * @param isTable true=数据表 false=数据库
     * @param sqlText SQL语句
     * @return 成功=true 失败=false
     */
    boolean setTableSQL(String rid, boolean isTable, String sqlText);

    /**
     * 根据资源名称与数据类型获取节点映射规则
     * @param rid 资源名称
     * @return 映射规则对象
     */
    DataRule getRule(String rid);

    /**
     * 获取数据节点映射规则列表
     * @param params 查询参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 规则对象列表
     * @return 符合条件的规则总数
     */
    int getRuleList(Map<String, Object> params, int start, int count, List<DataRule> rlist);
}
