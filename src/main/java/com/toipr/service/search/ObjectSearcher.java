package com.toipr.service.search;

import java.util.List;
import java.util.Map;

public interface ObjectSearcher {
    /**
     * 初始化对象索引器
     * @param hosts 服务器地址
     * @param args 其它参数
     * @return 成功=true 失败=false
     */
    boolean init(String[] hosts, Object... args);

    /**
     * 数据对象doid是否存在
     * @param doid 对象ID
     * @return true=存在
     */
    boolean exists(String doid);

    /**
     * 获取数据对象
     * @param doid 对象ID
     * @return JSONObject实例
     */
    Object getObject(String doid);

    /**
     * 统计匹配条件的记录数量
     * @param params 参数表
     * @return 成功=记录数量 失败=-1
     */
    int count(Map<String, Object> params);

    /**
     * 根据参数查询符合条件的若干条记录
     * @param params 参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param sort 排序字段
     * @return 成功=记录数量 失败=-1
     */
    int queryObjects(Map<String, Object> params, int start, int count, SortField sort, List rlist);

    /**
     * 根据参数查询符合条件的若干条记录
     * @param params 参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param sortList 排序字段列表, 多字段排序
     * @return 成功=记录数量 失败=-1
     */
    int queryObjects(Map<String, Object> params, int start, int count, List<SortField> sortList, List rlist);
}
