package com.toipr.mapper.org;

import com.toipr.model.user.OrgInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrgInfoMapper {
    /**
     * 添加机构对象
     * @param item 机构对象
     * @param table 数据表名称
     * @return 成功返回1
     */
    int addOrg(@Param("item") OrgInfo item, @Param("table") String table);

    /**
     * 设置机构状态
     * @param oid 机构ID
     * @param state 机构状态
     * @param table 数据表名称
     * @return 成功=1 失败=0
     */
    int setState(@Param("oid") String oid, @Param("state") int state, @Param("table") String table);

    /**
     * 删除机构对象
     * @param oid 机构代码
     * @param table 数据表名称
     * @return 成功=1
     */
    int removeOrg(@Param("oid") String oid, @Param("table") String table);

    /**
     * 机构是否存在
     * @param name 机构名称
     * @param pid 父机构ID
     * @param table 数据表名称
     * @return 成功=机构ID，失败=null
     */
    String orgExists(@Param("name") String name, @Param("pid") String pid, @Param("table") String table);

    /**
     * 获取机构对象
     * @param oid 机构ID
     * @param table 数据表名称
     * @return 机构对象实例，失败返回null
     */
    OrgInfo getOrg(@Param("oid") String oid, @Param("table") String table);

    /**
     * 统计符合条件的记录数
     * @param params 参数表
     * @param table 数据表名称
     * @return 记录数
     */
    int count(@Param("params") Map<String, Object> params, @Param("table") String table);

    /**
     * 获取机构列表
     * @param params 参数表
     * @param start 开始记录号
     * @param count 获取记录数
     * @param table 数据表名称
     * @return 机构列表
     */
    List<OrgInfo> getOrgList(@Param("params") Map<String, Object> params, @Param("start") int start, @Param("count") int count, @Param("table") String table);
}
