package com.toipr.client;

import java.util.List;
import java.util.Map;

public interface DownloadClient extends DataClient {
    /**
     * 下载数据对象，存放到path目录
     * @param rid 资源ID
     * @param uuid 数字对象ID
     * @param path 存放路径
     * @return 任务句柄
     */
    Object download(String rid, String uuid, String path);

    /**
     * 下载数据块
     * @param rid 资源ID
     * @param uuid 数据块ID
     * @param hash 数据校验码
     * @return 数据数组
     */
    byte[] getBlobData(String rid, String uuid, String hash);
}
