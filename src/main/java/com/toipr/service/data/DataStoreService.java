package com.toipr.service.data;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataObject;
import com.toipr.model.data.DataBlobRef;

import java.io.InputStream;
import java.util.List;

/**
 * 数据存储服务
 */
public interface DataStoreService {
    /**
     * 获取数据对象描述信息
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return 数据对象
     */
    DataObject getObject(String rid, String oid, String uuid);

    /**
     * 获取数据对象ID串
     * @param rid 资源ID
     * @param uuid 数据对象ID串
     * @return 数据块ID串对象
     */
    List<DataBlobRef> getBlobIds(String rid, String uuid);

    /**
     * 获取数据对象oid的数据块bid
     * @param rid 资源ID
     * @param bid 数据块ID
     * @return 数据块对象
     */
    DataBlob getBlob(String rid, String bid);

    /**
     * 添加数据对象doid的数据块
     * @param rid 资源ID
     * @param oid 拥有者ID，一般为用户ID
     * @param uuid 数字对象ID
     * @param flags 数据块处理标志
     * @param index 块序号
     * @param hash 数据HASH码，可能为空
     * @param data 字节数组
     * @return true=成功
     */
    boolean addBlob(String rid, String oid, String uuid, int flags, int index, String hash, byte[] data);

    /**
     * 获取数据对象的输入流
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return InputStream对象实例
     */
    InputStream getInputStream(String rid, String oid, String uuid);

    /**
     * 增加数据对象下载计数与设置最后下载时间
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return true=执行成功
     */
    boolean incObjectDown(String rid, String oid, String uuid);

    /**
     * 设置数据对象状态
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @param state 对象状态
     * @return true=成功 false=失败
     */
    boolean setState(String rid, String oid, String uuid, int state);

    /**
     * 删除数据对象
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return true=执行成功
     */
    boolean removeObject(String rid, String oid, String uuid);

    /**
     * 存储空数据对象，一般为目录对象
     * @param fobj 数据对象
     * @return 成功=true 失败=false
     */
    boolean storeObject(DataObject fobj);

    /**
     * 存储文件数据
     * @param sFile 文件路径
     * @param fobj 数据对象
     * @return true=执行成功
     */
    boolean storeObject(String sFile, DataObject fobj);

    /**
     * 存储二进制数组
     * @param buff 数据数组
     * @param fobj 数据对象
     * @return true=执行成功
     */
    boolean storeObject(byte[] buff, DataObject fobj);

    /**
     * 存储二进制数组
     * @param buff 数据数组
     * @param off 数据偏移
     * @param len 数据长度
     * @param fobj 数据对象
     * @return true=执行成功
     */
    boolean storeObject(byte[] buff, int off, int len, DataObject fobj);

    /**
     * 保存二进制流
     * @param ins 输入流
     * @param fobj 数据对象
     * @return true=执行成功
     */
    boolean storeObject(InputStream ins, DataObject fobj);
}
