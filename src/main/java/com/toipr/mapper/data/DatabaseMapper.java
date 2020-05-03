package com.toipr.mapper.data;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 检查与创建数据库
 */
public interface DatabaseMapper {
    /**
     * 判断数据库是否存在
     * @param dbName 数据库名称
     * @return 存在=1 不存在=0
     */
    int dbExists(@Param("dbName") String dbName);

    /**
     * 创建数据库
     * @param dbName 数据库名称
     * @return 成功=1 失败=0
     */
    int dbCreate(@Param("dbName") String dbName);

    /**
     * 获取已创建数据表名称列表
     * @param dbName 数据表名称
     * @return 表名列表
     */
    List<String> getTables(@Param("dbName") String dbName);
}
