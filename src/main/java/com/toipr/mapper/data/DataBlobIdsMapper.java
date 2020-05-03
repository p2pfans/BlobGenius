package com.toipr.mapper.data;

import com.toipr.model.data.DataBlobIds;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataBlobIdsMapper {
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
     * 新增文件数据块ID串
     * @param item 文件数据块ID串对象
     * @param table 数据表名称
     * @return 受影响的记录数
     */
    int addObject(@Param("item") DataBlobIds item, @Param("table") String table);

    /**
     * 根据uuid查询数据表table的数据块关联对象
     * @param uuid 数字资源ID
     * @param boid 数据块IDuuid
     * @param table 数据表名称
     * @return 返回FileBlob对象
     */
    DataBlobIds getObject(@Param("uuid") String uuid, @Param("boid") String boid, @Param("table") String table);

    /**
     * 获取数字对象uuid的关联对象列表
     * @param uuid 数字资源ID
     * @param table 数据表名称
     * @return 成功=数据块关联对象列表
     */
    List<DataBlobIds> getObjects(@Param("uuid") String uuid, @Param("table") String table);

    /**
     * 数字对象uuid的关联数据块是否存在
     * @param uuid 数字资源ID
     * @param table 数据表名称
     * @return 成功=数据块数量
     */
    int objectExists(@Param("uuid") String uuid, @Param("table") String table);

    /**
     * 数据块关联记录是否存在
     * @param uuid 数字资源ID
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 成功=1 失败=0
     */
    int blobExists(@Param("uuid") String uuid, @Param("boid") String boid, @Param("table") String table);

    /**
     * 删除数据表table的uuid数据块索引
     * @param uuid 数字资源ID
     * @param table 数据表名称
     * @return 受影响的记录数
     */
    int removeObject(@Param("uuid") String uuid, @Param("table") String table);

    /**
     * 删除数据块索引
     * @param uuid 数字资源ID
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 成功=1 失败=0
     */
    int removeBlob(@Param("uuid") String uuid, @Param("boid") String boid, @Param("table") String table);
}
