package com.toipr.service.server;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataObject;
import com.toipr.model.data.DataBlobIds;

import java.util.Date;
import java.util.List;

/**
 * 数据服务器对象
 * 负责存取数据对象机器关联描述与数据
 */
public interface DataServer {
    /**
     * 数据节点类型
     */
    int prevNode = -1;
    int nextNode = 1;
    int primary = 2;

    /**
     * 责任链传导方向 direction
     */
    int noChain = 0;        //禁止传导
    int prevChain = 1;      //前向传导
    int nextChain = 2;      //后向传导
    int biChain = 3;        //双向传导
    int priChain = 4;       //主节点传导
    int allChain = 7;       //三向传导
    int jobChain = 1024;     //正在执行责任传导

    /**
     * 设置下一个处理的数据服务器，责任链设计模式
     * 解决数据多节点复制 与 失败多点重试问题
     * @param type -1=前一个 1=后一个 0=主服务器
     * @param server 数据服务器
     */
    void setServer(int type, DataServer server);

    /**
     * 获取服务器节点配置DataNode
     * @return DataNode对象
     */
    Object getNode();

    /**
     * 获取数据映射规则DataRule
     * @return DataRule对象
     */
    Object getRule();

    /**
     * 数据块是否存在
     * @param boid 数据块ID
     * @return 数据记录号，不唯一，大于0成功
     */
    Object blobExists(String boid);
    Object blobExists(String boid, int direction);

    /**
     * 根据ID获取数据块
     * @param boid 数据块ID
     * @return 数据块对象
     */
    DataBlob getBlob(String boid);

    /**
     * 根据ID获取数据块
     * @param boid 数据块ID
     * @param isCopy 是否直接复制，不处理解压/解密等操作
     * @param direction 操作传导方向
     * @return 数据块对象
     */
    DataBlob getBlob(String boid, boolean isCopy, int direction);

    /**
     * 添加数据块
     * @param blob 数据块对象
     * @return 成功=1 失败=0
     */
    int addBlob(DataBlob blob);

    /**
     * 添加数据块
     * @param blob 数据块对象
     * @param isCopy 是否直接复制，不处理压缩/加密等
     * @param direction 传导方向
     * @return 成功=1 失败=0
     */
    int addBlob(DataBlob blob, boolean isCopy, int direction);

    /**
     * 根据ID删除数据块
     * @param boid 数据块ID
     * @return 影响记录数，大于0成功
     */
    int removeBlob(String boid);
    int removeBlob(String boid, int direction);

    /**
     * 增加数据块引用数
     * @param boid 数据块ID
     * @return 影响记录数，大于0成功
     */
    int incBlobRefs(String boid);
    int incBlobRefs(String boid, int direction);

    /**
     * 减少数据块引用数
     * @param boid 数据块ID
     * @return 影响记录数，大于0成功
     */
    int decBlobRefs(String boid);
    int decBlobRefs(String boid, int direction);

    /**
     * 增加下载次数，设置最后访问时间
     * @param boid 数据块ID
     * @param lastAccess 访问时间戳
     * @return 影响记录数,成功等于1
     */
    int incBlobDown(String boid, Date lastAccess);
    int incBlobDown(String boid, Date lastAccess, int direction);

    /**
     * 判断数据对象的数据块ID串是否存在
     * @param uuid 数字对象ID
     * @param boid 数据块ID
     * @return 数据记录号，不唯一，大于0成功
     */
    Object blobIdsExists(String uuid, String boid);
    Object blobIdsExists(String uuid, String boid, int direction);

    /**
     * 添加数据对象的数据块ID串
     * @param obj 数据对象ID串
     * @return 数据记录号，不唯一，大于0成功
     */
    int addBlobIds(DataBlobIds obj);
    int addBlobIds(DataBlobIds obj, int direction);

    /**
     * 删除数据对象ID串
     * @param uuid 文件对象ID
     * @return 数据记录号，不唯一，大于0成功
     */
    int removeBlobIds(String uuid);
    int removeBlobIds(String uuid, int direction);

    /**
     * 根据数据对象ID获取数据块ID串
     * @param uuid 数据对象ID
     * @param boid 数据块ID
     * @return 数据记录号，不唯一，大于0成功
     */
    DataBlobIds getBlobIds(String uuid, String boid);
    DataBlobIds getBlobIds(String uuid, String boid, int direction);

    /**
     * 获取数字对象doid的数据块索引列表
     * @param uuid 数字对象ID
     * @return 成功=数据块索引列表
     */
    List<DataBlobIds> getBlobIds(String uuid);
    List<DataBlobIds> getBlobIds(String uuid, int direction);

    /**
     * 数据对象是否存在
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @return 数据记录号，不唯一，大于0成功
     */
    Object objectExists(String oid, String uuid);
    Object objectExists(String oid, String uuid, int direction);

    /**
     * 添加数据对象
     * @param obj 数据对象
     * @return 成功=1 失败=0
     */
    int addObject(DataObject obj);
    int addObject(DataObject obj, int direction);

    /**
     * 根据ID删除数据对象
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @return 成功=1 失败=0
     */
    int removeObject(String oid, String uuid);
    int removeObject(String oid, String uuid, int direction);

    /**
     * 根据ID获取数据对象
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @return 数据对象
     */
    DataObject getObject(String oid, String uuid);
    DataObject getObject(String oid, String uuid, int direction);

    /**
     * 设置梳子对象状态
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @param state 对象状态
     * @return 成功=1 失败=0
     */
    int setState(String oid, String uuid, int state);
    int setState(String oid, String uuid, int state, int direction);

    /**
     * 增加文件下载计数，设置最后下载时间
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @param lastAccess 最后下载时间
     * @return 成功=1 失败=0
     */
    int incObjectDown(String oid, String uuid, Date lastAccess);
    int incObjectDown(String oid, String uuid, Date lastAccess, int direction);
}
