package com.toipr.mapper.oacl;

import com.toipr.model.oacl.ObjectAccessGroup;
import org.apache.ibatis.annotations.Param;

public interface ObjectAccessGroupMapper {
    /**
     * 添加访问控制组
     * @param item 组对象
     * @param table 数据表名称
     * @return 返回受影响行数，成功返回1
     */
    int addGroup(@Param("item") ObjectAccessGroup item, @Param("table") String table);

    /**
     * 根据gid获取访问控制组对象
     * @param gid 组ID
     * @param table 数据表名称
     * @return 访问控制组对象
     */
    ObjectAccessGroup getGroup(@Param("gid") String gid, @Param("table") String table);

    /**
     * 删除访问控制组
     * @param gid
     * @param table
     * @return
     */
    int removeGroup(@Param("gid") String gid, @Param("table") String table);

    /**
     * 访问控制组是否存在
     * @param name 组名称
     * @param uid 用户ID
     * @param table 数据表名称
     * @return 成功返回记录ID，失败返回null
     */
    Object groupExists(@Param("name") String name, @Param("uid") String uid, @Param("table") String table);
}
