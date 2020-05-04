package com.toipr.service.search;

public interface NodeManager {
    /**
     * 根据资源获取对象索引器
     * @param resid 资源ID
     * @return 对象索引器
     */
    ObjectIndexer getIndexer(String resid);

    /**
     * 根据资源获取对象搜索器
     * @param resid 资源ID
     * @return 对象搜索器
     */
    ObjectSearcher getSearcher(String resid);
}
