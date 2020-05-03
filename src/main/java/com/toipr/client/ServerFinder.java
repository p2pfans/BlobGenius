package com.toipr.client;

public interface ServerFinder {
    /**
     * 用户验证
     * @param uname 用户账号
     * @param upass 用户密码
     * @param args 其它参数
     * @return true=成功 false=失败
     */
    boolean auth(String uname, String upass, Object... args);

    /**
     * 根据资源与数据类型获取可用服务器地址
     * @param rid 资源ID
     * @param type 数据类型，objects=数字对象, blobs=数据块
     * @param isUpdate true=更新 false=读取
     * @return 服务器地址
     */
    String getServer(String rid, String type, boolean isUpdate);
}
