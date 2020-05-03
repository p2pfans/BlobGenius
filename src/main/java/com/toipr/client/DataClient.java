package com.toipr.client;

import java.util.Map;

public interface DataClient {
    /**
     * 关闭下载客户端
     */
    void close();

    /**
     * 取消任务
     * @param task
     */
    void cancel(Object task);

    /**
     * 添加进度事件监听器
     * @param listener
     */
    void addListener(ProgressListener listener);

    /**
     * 用户验证
     * @param uname 用户账号
     * @param upass 用户密码
     * @param args 其它参数
     * @return true=成功 false=失败
     */
    boolean auth(String uname, String upass, Object... args);

    /**
     * 获取对象属性
     * @param target 对象实例
     * @return 属性映射表
     */
    Map<String, Object> getObjectMap(Object target);
}
