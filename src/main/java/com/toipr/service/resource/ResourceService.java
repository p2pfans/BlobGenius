package com.toipr.service.resource;

import com.toipr.model.data.DataResource;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ResourceService {
    /**
     * 添加数据资源
     * @param obj 资源对象
     * @return 成功=true 失败=false
     */
    boolean addResource(DataResource obj);

    /**
     * 设置主机状态
     * @param rid 主机ID
     * @param state 工作状态
     * @return true=成功 false=失败
     */
    boolean setState(String rid, int state);

    /**
     * 判断用户oid资源code是否存在
     * @param oid 拥有者ID
     * @param code 资源内部代码，如java package命名方式
     * @return 成功=资源ID 失败=null
     */
    String resourceExists(String oid, String code);

    /**
     * 删除用户oid的资源code
     * @param oid 用户ID
     * @param rid 资源ID
     * @return 成功=true 失败=false
     */
    boolean removeResource(String oid, String rid);

    /**
     * 获取资源对象rid
     * @param rid 资源ID
     * @return 资源对象实例
     */
    DataResource getResource(String rid);

    /**
     * 获取用户oid的资源code
     * @param oid 用户ID
     * @param code 资源代码
     * @return 资源对象实例
     */
    DataResource getResource(String oid, String code);

    /**
     * 获取用户oid的资源列表
     * @param params 查询条件
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 资源列表
     * @return 记录总数
     */
    int getResourceList(Map<String, Object> params, int start, int count, List<DataResource> rlist);

    /**
     * 设置资源条目数
     * @param rid 资源ID
     * @param totalCount 资源总条目数
     * @return 成功=true 失败=false
     */
    boolean setTotalCount(String rid, long totalCount);

    /**
     * 设置资源存储容量
     * @param rid 资源ID
     * @param totalSpace 资源存储容量，单位字节
     * @return 成功=true 失败=false
     */
    boolean setTotalSpace(String rid, long totalSpace);

    /**
     * 增加总访问次数，设置最后访问时间
     * @param rid 资源ID
     * @param delta 访问次数增量
     * @param lastAccess 最后访问时间
     * @return 成功=true 失败=false
     */
    boolean incVisitAndLastAccess(String rid, int delta, Date lastAccess);
}
