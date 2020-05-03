package com.toipr.mapper.data;

import com.toipr.model.data.DataObject;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface DataObjectMapper {
    /**
     * 创建数据表
     * @param tblName 数据表名称
     * @return 成功=1 失败=0
     */
    int createTable(@Param("tblName") String tblName);

    /**
     * 判断数据表是否存在
     * @param dbName 数据库名称
     * @param tblName 数据表名称
     * @return 存在=1 不存在=0
     */
    int tableExists(@Param("dbName") String dbName, @Param("tblName") String tblName);

    /**
     * 添加数据对象到数据表table
     * @param item 数据对象
     * @param table 数据表名称
     * @return 成功返回1
     */
    int addObject(@Param("item") DataObject item, @Param("table") String table);

    /**
     * 根据ID获取数据对象
     * @param uuid 数据对象ID
     * @param table 数据表名称
     * @return 成功返回数据对象实例，失败返回null
     */
    DataObject getObject(@Param("uuid") String uuid, @Param("table") String table);

    /**
     * 判断文件fid是否存在于table数据表中
     * @param uuid 数据对象ID
     * @param table 数据表名称
     * @return 返回文件自增记录，表内唯一，可能与其他表重复
     */
    Object objectExists(@Param("uuid") String uuid, @Param("table") String table);

    /**
     * 从数据表table中删除数据对象fid
     * @param uuid 数据对象ID
     * @param table 数据表名称
     * @return 成功返回影响记录数
     */
    int removeObject(@Param("uuid") String uuid, @Param("table") String table);

    /**
     * 设置数据对象访问状态
     * @param uuid 数据对象ID
     * @param state 访问状态，如禁用、待删等
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setState(@Param("uuid") String uuid, @Param("state") int state, @Param("table") String table);

    /**
     * 设置数据对象访问状态
     * @param uuid 数据对象ID
     * @param flags 对象属性，如压缩、加密等
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setFlags(@Param("uuid") String uuid, @Param("flags") int flags, @Param("table") String table);

    /**
     * 设置数据对象访问状态
     * @param uuid 数据对象ID
     * @param oid 拥有者ID
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setOid(@Param("uuid") String uuid, @Param("oid") String oid, @Param("table") String table);

    /**
     * 设置数据对象的父对象
     * @param uuid 数据对象ID
     * @param pid 父数据对象ID
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setPid(@Param("uuid") String uuid, @Param("pid") String pid, @Param("table") String table);

    /**
     * 设置数据对象的所属资源
     * @param uuid 数据对象ID
     * @param rid 资源ID
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setRid(@Param("uuid") String uuid, @Param("rid") String rid, @Param("table") String table);

    /**
     * 增加下载次数，设置最后访问时间
     * @param uuid 数据对象ID
     * @param lastAccess 访问时间戳
     * @param table 数据表名称
     * @return 影响记录数,成功等于1
     */
    int incObjectDown(@Param("uuid") String uuid, @Param("lastAccess") Date lastAccess, @Param("table") String table);
}
