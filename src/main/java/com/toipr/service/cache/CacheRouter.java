package com.toipr.service.cache;

import com.toipr.model.node.CacheNode;

public interface CacheRouter {
    /**
     * 添加缓存节点
     * @param node 节点对象
     * @return 成功=true 失败=false
     */
    boolean addNode(CacheNode node);

    /**
     * 判断映射主键是否在本节点缓存
     * @param distKey 映射主键
     * @return true=是 false=否
     */
    boolean inThisNode(String distKey);

    /**
     * 获取缓存映射主键
     * @param dbname 数据库名
     * @param key 映射主键
     * @param userData 自定义参数
     * @return 缓存映射主键
     */
    String getRouterKey(String dbname, String key, Object userData);

    /**
     * 根据映射主键获取缓存服务器
     * @param distKey 缓存主键
     * @param isUpdate true=更新操作
     * @param userData 自定义对象
     * @return 缓存服务器
     */
    CacheServer getServer(String distKey, boolean isUpdate, Object userData);

    /**
     * 根据数据库名、缓存主键与自定义参数获取缓存服务器
     * @param dbname 数据库名称
     * @param key 缓存主键
     * @param isUpdate true=更新操作
     * @param userData 自定义对象
     * @return 缓存服务器
     */
    CacheServer getServer(String dbname, String key, boolean isUpdate, Object userData);
}
