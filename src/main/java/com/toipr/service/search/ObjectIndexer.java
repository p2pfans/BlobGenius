package com.toipr.service.search;

import com.toipr.service.cache.MessageHandler;

import java.util.List;

public interface ObjectIndexer extends MessageHandler{
    /**
     * 初始化对象索引器
     * @param hosts 服务器地址
     * @param args 其它参数
     * @return 成功=true 失败=false
     */
    boolean init(String[] hosts, Object... args);

    /**
     * 索引一个对象
     * @param obj 对象实例
     * @return 成功=true 失败=false
     */
    boolean index(Object obj);

    /**
     * 索引对象列表
     * @param oList 对象列表
     * @return 成功索引记录数量
     */
    int index(List oList);

    /**
     * 删除索引对象
     * @param oid 对象ID
     */
    void remove(String oid);
}
