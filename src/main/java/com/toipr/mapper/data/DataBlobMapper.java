package com.toipr.mapper.data;

import com.toipr.model.data.DataBlob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface DataBlobMapper {
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
     * 插入DataBlob对象，返回记录ID
     * @param blob 数据块对象
     * @param table 数据表名
     * @return 返回记录ID
     */
    int addBlob(@Param("blob") DataBlob blob, @Param("table") String table);

    /**
     * 根据ID获取数据块内容
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 数据块对象
     */
    DataBlob getBlob(@Param("boid") String boid, @Param("table") String table);

    /**
     * 根据ID获取数据块描述数据，不需要获取数据内容
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 数据块对象
     */
    DataBlob getBlobMeta(@Param("boid") String boid, @Param("table") String table);

    /**
     * 判断数据块是否存在
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 记录ID 或 null
     */
    Object blobExists(@Param("boid") String boid, @Param("table") String table);

    /**
     * 删除数据块
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 影响记录数,成功等于1
     */
    int removeBlob(@Param("boid") String boid, @Param("table") String table);

    /**
     * 删除ID列表规定的数据块
     * @param idList 数据块ID列表
     * @param table 数据表名称
     * @return 影响记录数,成功>0
     */
    int removeAllBlobs(@Param("idList")List<String> idList, @Param("table") String table);

    /**
     * 增加数据块的引用计数1
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 影响记录数,成功等于1
     */
    int incBlobRefs(@Param("boid") String boid, @Param("table") String table);

    /**
     * 减少数据块的引用计数1
     * @param boid 数据块ID
     * @param table 数据表名称
     * @return 影响记录数,成功等于1
     */
    int decBlobRefs(@Param("boid") String boid, @Param("table") String table);

    /**
     * 减少ID列表rlist中所有数据块的引用计数
     * @param rlist 数据块ID列表
     * @param table 数据表名称
     * @return  影响记录数,成功等于1
     */
    int decAllBlobRefs(@Param("rlist") List<String> rlist, @Param("table") String table);

    /**
     * 增加下载次数，设置最后访问时间
     * @param boid 数据块ID
     * @param lastAccess 访问时间戳
     * @return 影响记录数,成功等于1
     */
    int incBlobDown(@Param("boid") String boid, @Param("lastAccess") Date lastAccess, @Param("table") String table);
}
