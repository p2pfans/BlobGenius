package com.toipr.mapper.resource;

import com.toipr.model.data.DataResource;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataResourceMapper {
    /**
     * 添加数据资源
     * @param item 资源对象
     * @return 成功返回1
     */
    int addResource(@Param("item") DataResource item);

    /**
     * 设置主机状态
     * @param rid 主机ID
     * @param state 工作状态
     * @return true=成功 false=失败
     */
    int setState(@Param("rid") String rid, @Param("state") int state);

    /**
     * 统计用户oid拥有的资源总数
     * @param params 查询条件参数表
     * @return 资源数量
     */
    int countResource(@Param("params") Map<String, Object> params);

    /**
     * 删除数据资源
     * @param rid 资源ID
     * @return 成功返回1
     */
    int removeResource(@Param("rid") String rid);

    /**
     * 判断资源是否存在
     * @param oid 拥有者ID
     * @param code 资源代码
     * @return 成功=记录ID，失败=null
     */
    String resourceExists(@Param("oid") String oid, @Param("code") String code);

    /**
     * 根据ID获取资源对象
     * @param rid 拥有者ID
     * @return 成功返回资源对象实例，失败返回null
     */
    DataResource getResource(@Param("rid") String rid);

    /**
     * 根据拥有者与资源代码获取资源对象
     * @param oid 拥有者ID
     * @param code 资源代码
     * @return 成功返回资源对象实例，失败返回null
     */
    DataResource getResource2(@Param("oid") String oid, @Param("code") String code);

    /**
     * 根据拥有者与状态获取资源列表
     * @param params 查询条件参数表
     * @param start 开始记录
     * @param count 记录数量
     * @return 资源列表
     */
    List<DataResource> getAllResources(@Param("params") Map<String, Object> params, @Param("start") int start, @Param("count") int count);

    /**
     * 设置资源条目数
     * @param rid 资源ID
     * @param totalCount 资源总条目数
     * @return 受影响行数，成功返回1
     */
    int setTotalCount(@Param("rid") String rid, @Param("totalCount") long totalCount);

    /**
     * 设置资源存储容量
     * @param rid 资源ID
     * @param totalSpace 资源存储容量，单位字节
     * @return 受影响行数，成功返回1
     */
    int setTotalSpace(@Param("rid") String rid, @Param("totalSpace") long totalSpace);

    /**
     * 增加总访问次数，设置最后访问时间
     * @param rid 资源ID
     * @param delta 访问次数增量
     * @param lastAccess 最后访问时间
     * @return 受影响行数，成功返回1
     */
    int incVisitAndLastAccess(@Param("rid") String rid, @Param("delta") int delta, @Param("lastAccess") Date lastAccess);
}
