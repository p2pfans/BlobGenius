package com.toipr.service.data;

import com.toipr.model.data.DataBlob;

/**
 * 数据块存储对象
 * 每个存储对象属于一个资源类型
 */
public interface BlobStore {
    /**
     * 初始化文件存储
     * @param rid 资源ID
     * @param args 自定义参数
     * @return true=成功
     */
    boolean init(String rid, Object... args);

    /**
     * 读取数据块数据
     * @param doid 数据块ID
     * @return 数据数组
     */
    byte[] getData(String doid);

    /**
     * 读取数据块
     * @param doid 数据块ID
     * @return DataBlob实例
     */
    DataBlob getBlob(String doid);

    /**
     * 存储数据块对象
     * @param blob
     * @return true=成功
     */
    boolean saveBlob(DataBlob blob);

    /**
     * 数据块是否存在
     * @param doid 数据块ID
     * @return true=存在
     */
    boolean blobExists(String doid);

    /**
     * 删除数据块
     * @param doid 数据块ID
     * @return true=成功
     */
    boolean removeBlob(String doid);
}
