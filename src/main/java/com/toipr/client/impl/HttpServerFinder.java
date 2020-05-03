package com.toipr.client.impl;

import com.toipr.client.ServerFinder;

public class HttpServerFinder implements ServerFinder {
    protected String server;
    public HttpServerFinder(String server){
        this.server = server;
    }

    /**
     * 用户验证
     * @param uname 用户账号
     * @param upass 用户密码
     * @param args 其它参数
     * @return true=成功 false=失败
     */
    public boolean auth(String uname, String upass, Object... args){
        //TODO 后期实现用户验证
        return true;
    }

    /**
     * 根据资源与数据类型获取可用服务器地址
     * @param rid 资源ID
     * @param type 数据类型，objects=数字对象, blobs=数据块
     * @param isUpdate true=更新 false=读取
     * @return 服务器地址
     */
    public String getServer(String rid, String type, boolean isUpdate){
        //TODO 后期实现不同资源与数据类型，从服务器动态获取可用服务器
        return this.server;
    }
}
