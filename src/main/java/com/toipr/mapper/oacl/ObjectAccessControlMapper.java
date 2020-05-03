package com.toipr.mapper.oacl;

import com.toipr.model.oacl.ObjectAccessControl;
import org.apache.ibatis.annotations.Param;

public interface ObjectAccessControlMapper {
    /**
     * 添加对象访问控制
     * @param oacl 访问控制
     * @param table 数据表名
     * @return 返回受影响的行数，成功返回大于0值
     */
    int addAccessControl(@Param("oacl") ObjectAccessControl oacl, @Param("table") String table);

    /**
     * 获取访问控制对象
     * @param oid 用户ID/用户组ID
     * @param doid 数据对象ID/资源对象ID
     * @param table 数据表名
     * @return 访问控制对象实例，失败返回null
     */
    ObjectAccessControl getAccessControl(@Param("oid") String oid, @Param("doid") String doid, @Param("table") String table);

    /**
     * 访问控制对象是否存在
     * @param oid 用户ID/用户组ID
     * @param doid 数据对象ID/资源对象ID
     * @param table 数据表名
     * @return 成功返回记录id，失败返回null
     */
    Object accessControlExists(@Param("oid") String oid, @Param("doid") String doid, @Param("table") String table);

    /**
     * 删除访问控制对象
     * @param oid 用户ID/用户组ID
     * @param doid 数据对象ID/资源对象ID
     * @param table 数据表名
     * @return 返回受影响的行数，成功返回大于0值
     */
    int removeAccessControl(@Param("oid") String oid, @Param("doid") String doid, @Param("table") String table);
}
