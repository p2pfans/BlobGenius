package com.toipr.service.rule;

import com.toipr.model.data.DataRule;

public interface RuleRouter {
    /**
     * 初始化数据路由规则
     * @param nodeRule 节点路由规则
     * @param tableRule 数据表路由规则
     * @return 成功=true 失败=false
     */
    boolean init(String nodeRule, String tableRule);

    /**
     * 生成节点存储映射主键
     * @param doid 数字对象ID
     * @param oid 拥有者ID
     * @param userData 其它自定义参数
     * @return 节点映射主键
     */
    String getNodeKey(String doid, String oid, Object userData);

    /**
     * 生成数据表映射主键
     * @param doid 数字对象ID
     * @param oid 拥有者ID
     * @param userData 其它自定义参数
     * @return 数据表映射主键
     */
    String getTableKey(String doid, String oid, Object userData);
}
