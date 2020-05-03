package com.toipr.service.cache;

public interface MessageHandler {
    /**
     * 消息处理回调
     * @param tid 任务ID
     * @param channel 频道名称
     * @param message 消息体
     */
    void onMessage(String tid, String channel, String message);

    /**
     * 消息处理回调
     * @param tid 任务ID
     * @param pattern 频道模式
     * @param channel 频道名称
     * @param message 消息体
     */
    void onMessage(String tid, String pattern, String channel, String message);
}
