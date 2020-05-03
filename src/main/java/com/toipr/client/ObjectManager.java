package com.toipr.client;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ObjectManager extends DataClient {
    /**
     * 创建目录对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param flags 目录属性
     * @param name 对象名称
     * @param path  目录路径
     * @return 目录对象
     */
    Object createDir(String rid, String pid, int flags, String name, String path);

    /**
     * 根据文件生成数字对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param flags 对象标志
     * @param name 对象名称
     * @param path  目录路径
     * @param file 文件对象
     * @return JSONObject
     */
    Object createObject(String rid, String pid, int flags, String name, String path, File file);

    /**
     * 根据参数生成数字对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param flags 对象标志
     * @param name 对象名称
     * @param path  目录路径
     * @param hash 数据校验码
     * @param size 对象大小
     * @param blobSize 数据块大小
     * @param mime 数据类型
     * @return JSONObject
     */
    Object createObject(String rid, String pid, int flags, String name, String path, String hash, long size, int blobSize, String mime);

    /**
     * 根据参数生成数字对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param params 对象属性
     * @return JSONObject
     */
    Object createObject(String rid, String pid, Map<String, Object> params);

    /**
     * 获取数字对象的块索引队列, 支持断点续传功能
     * @param rid 资源ID
     * @param uuid 数字对象ID
     * @return 块索引队列JSONObject
     */
    List<Object> getBlobIds(String rid, String uuid);

    /**
     * 删除对象
     * @param rid 资源ID
     * @param uuid 对象ID
     * @return true=成功 false=失败
     */
    boolean removeObject(String rid, String uuid);

    /**
     * 设置对象状态
     * @param rid 资源ID
     * @param uuid 对象ID
     * @param state 状态码
     * @return true=成功 false=失败
     */
    boolean setState(String rid, String uuid, int state);

    /**
     * 从服务器获取对象
     * @param rid 资源ID
     * @param uuid 对象ID
     * @return 对象实例
     */
    Object getObject(String rid, String uuid);

    /**
     * 根据参数获取对象列表
     * @param params 参数列表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 记录列表
     * @return 记录总数
     */
    int queryObjects(Map<String, Object> params, int start, int count, List<Object> rlist);
}
