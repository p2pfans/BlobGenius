package com.toipr.service.cache;

/**
 * 统一使用线程池处理REDIS发布订阅消息
 */
public interface CacheSubscriber {
    /**
     * 关闭订阅管理器
     */
    void close();

    /**
     * 取消订阅
     * @param tid 任务ID
     */
    void unsubscribe(String tid);

    /**
     * 订阅消息频道channel
     * @param name 频道名称
     * @param isChannel true=频道名称 false=名称模式
     * @param handler 消息处理器
     * @return 成功=订阅ID 失败=null
     */
    String subscribe(String name, boolean isChannel, MessageHandler handler);
}
