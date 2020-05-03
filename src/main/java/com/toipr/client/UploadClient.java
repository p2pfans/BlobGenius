package com.toipr.client;

import java.util.List;

public interface UploadClient extends DataClient {
    /**
     * 上传文件或目录
     * @param rid 资源ID
     * @param pid 目录ID
     * @param path 文件路径
     * @return 任务句柄
     */
    Object upload(String rid, String pid, String path);

    /**
     * 上传数据块
     * @param rid 资源ID
     * @param doid 数字对象ID
     * @param index 块序号
     * @param data 数据数组
     * @return true=成功
     */
    boolean upload(String rid, String doid, int index, byte[] data);
}
