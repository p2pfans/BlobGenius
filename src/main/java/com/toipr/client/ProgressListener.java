package com.toipr.client;

public interface ProgressListener {
    /**
     * 设置进度事件监听器
     * @param listener
     */
    void setListener(ProgressListener listener);

    /**
     * 开始前通知，处理上传/下载前置操作
     * @param uuid 对象ID
     * @param name 对象名称
     * @param size 对象大小
     * @param isDown true=上传 false=下载
     * @param obj 对象实例或其他自定义参数
     * @return true=继续执行 false=终止执行
     */
    boolean beforeNotify(String uuid, String name, long size, boolean isDown, Object obj);

    /**
     * 过程通知，通报上传/下载进度
     * @param uuid 对象ID
     * @param name 对象名称
     * @param size 对象大小
     * @param done 完成大小
     * @param isDown true=上传 false=下载
     * @param obj 对象实例或其他自定义参数
     * @return true=继续执行 false=终止执行
     */
    boolean progressNotify(String uuid, String name, long size, long done, boolean isDown, Object obj);

    /**
     * 错误通知
     * @param uuid 对象ID
     * @param name 对象名称
     * @param isDown true=上传 false=下载
     * @param ex 异常对象
     * @param obj 对象实例或其他自定义参数
     * @return true=继续执行 false=终止执行
     */
    boolean errorNotify(String uuid, String name, boolean isDown, Exception ex, Object obj);

    /**
     * 完成通知
     * @param uuid 对象ID
     * @param name 对象名称
     * @param size 对象大小
     * @param isDown true=上传 false=下载
     * @param obj 对象实例或其他自定义参数
     */
    void completeNotify(String uuid, String name, long size, boolean isDown, Object obj);
}
