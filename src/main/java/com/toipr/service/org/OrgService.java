package com.toipr.service.org;

import com.toipr.model.user.OrgInfo;

import java.util.List;
import java.util.Map;

public interface OrgService {
    /**
     * 添加机构对象
     * @param obj 机构对象
     * @return 成功=true 失败=false
     */
    boolean addOrg(OrgInfo obj);

    /**
     * 设置机构状态
     * @param oid 机构ID
     * @param state 机构状态
     * @return 成功=true 失败=false
     */
    boolean setState(String oid, int state);

    /**
     * 删除机构对象
     * @param oid 机构ID
     * @return 成功=true 失败=false
     */
    boolean removeOrg(String oid);

    /**
     * 机构是否存在
     * @param name 机构名称
     * @param pid 父机构ID，可以为null
     * @return 存在=机构ID 不存在=null
     */
    String orgExists(String name, String pid);

    /**
     * 获取机构对象oid
     * @param oid 机构ID
     * @return 机构对象实例
     */
    OrgInfo getOrg(String oid);

    /**
     * 根据参数获取机构列表
     * @param params 参数映射表
     * @param start 开始记录
     * @param count 记录数量
     * @param olist 机构列表
     * @return 成功=记录数总数 失败=0
     */
    int getOrgList(Map<String, Object> params, int start, int count, List<OrgInfo> olist);
}
