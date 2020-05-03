package com.toipr.service.data.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataBlobRef;
import com.toipr.model.data.DataConst;
import com.toipr.model.data.DataObject;
import com.toipr.service.data.DataStoreService;
import com.toipr.service.server.DataNodeRouter;
import com.toipr.service.server.DataServer;
import com.toipr.service.server.DataServers;

import com.toipr.util.HashHelper;
import com.toipr.util.Utils;
import com.toipr.util.hash.HashConsumer;
import com.toipr.util.hash.HashProducer;
import com.toipr.util.hash.impl.DefaultHashProducer;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDataStoreService implements DataStoreService, HashConsumer{
    protected Map<String, Object> tLocals = new HashMap<String, Object>();

    protected HashProducer hashProducer = null;

    public DefaultDataStoreService(){
        hashProducer = new DefaultHashProducer();
        hashProducer.setConsumer(this);
    }

    /**
     * 获取数据对象
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return 数据对象
     */
    public DataObject getObject(String rid, String oid, String uuid){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_Object, uuid, oid, false, null);
        if(server==null){
            return null;
        }
        return server.getObject(oid, uuid);
    }

    /**
     * 删除数据对象
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return true=执行成功
     */
    public boolean removeObject(String rid, String oid, String uuid){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_Object, uuid, oid, true, null);
        if(server==null){
            return false;
        }

        if(server.removeObject(oid, uuid)>0) {
            /**
             * 删除数据对象，减少数据块引用计数
             */
            DataStoreHelper.decAllBlobRefs(rid, oid, uuid, null);
            return true;
        }
        return false;
    }

    /**
     * 获取数据对象ID串
     * @param rid 资源ID
     * @param uuid 数据对象ID串
     * @return 数据块ID串对象
     */
    public List<DataBlobRef> getBlobIds(String rid, String uuid){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_BlobRef, uuid, null, false, null);
        if(server==null){
            return null;
        }
        return server.getBlobIds(uuid);
    }

    /**
     * 获取数据对象oid的数据块bid
     * @param rid 资源ID
     * @param boid 数据块ID
     * @return 数据块对象
     */
    public DataBlob getBlob(String rid, String boid){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_Blob, boid, null, false, null);
        if(server==null){
            return null;
        }

        DataBlob blob = server.getBlob(boid);
        if(blob!=null){
            server.incBlobDown(boid, new Date());
        }
        return blob;
    }

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
    public boolean addBlob(String rid, String oid, String uuid, int flags, int index, String hash, byte[] data){
        try {
            if (Utils.isNullOrEmpty(hash)) {
                hash = HashHelper.computeHash(data, "SHA-256");
            }
            String boid = HashHelper.getShortHashStr(hash, 12);

            DataNodeRouter router = DataServers.getInstance();
            DataServer server = router.getServer(rid, DataConst.DataType_Blob, boid, null, true, null);
            if(server==null){
                return false;
            }

            DataBlobRef item = new DataBlobRef();
            item.setUuid(uuid);
            item.setBoid(boid);
            item.setHash(hash);
            item.setSerial(index);
            item.setSize(data.length);
            if(server.blobExists(boid)!=null){
                if(server.incBlobRefs(boid)==0){
                   return false;
                }
                return DataStoreHelper.addBlobIds(rid, oid, item);
            }

            DataBlob blob = new DataBlob();
            blob.setBoid(boid);
            blob.setFlags((short)flags);
            blob.setRefs(1);
            blob.setCopy(1);
            blob.setHash(hash);
            blob.setDownload(0);
            blob.setSize(data.length);
            blob.setData(data);
            blob.setTimeCreate(new Date());
            blob.setLastAccess(new Date());
            int ret = server.addBlob(blob);
            if(ret>0){
                if(!DataStoreHelper.addBlobIds(rid, oid, item)){
                    return false;
                }
            }
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 获取数据对象的输入流
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return InputStream对象实例
     */
    public InputStream getInputStream(String rid, String oid, String uuid){
        DataObject dobj = getObject(rid, oid, uuid);
        if(dobj==null){
            return null;
        }
        return new DefaultDataObjectInputStream(dobj);
    }

    /**
     * 增加数据对象下载计数与设置最后下载时间
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @return true=执行成功
     */
    public boolean incObjectDown(String rid, String oid, String uuid){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_Object, uuid, oid, true, null);
        if(server==null){
            return false;
        }
        return (server.incObjectDown(oid, uuid, new Date())>0);
    }

    /**
     * 设置数据对象状态
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数据对象ID
     * @param state 对象状态
     * @return true=成功 false=失败
     */
    public boolean setState(String rid, String oid, String uuid, int state){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_Object, uuid, oid, true, null);
        if(server==null){
            return false;
        }
        return server.setState(oid, uuid, state)>0;
    }

    /**
     * 存储空数据对象，一般为目录对象
     * @param fobj 数据对象
     * @return 成功=true 失败=false
     */
    public boolean storeObject(DataObject fobj){
        if(!DataStoreHelper.newDoidOfObject(fobj)){
            return false;
        }
        if(!DataStoreHelper.newUuidOfObject(fobj)) {
            return false;
        }
        return DataStoreHelper.saveObject(fobj);
    }

    /**
     * 存储文件数据
     * @param sFile 文件路径
     * @param fobj 数据对象
     * @return true=执行成功
     */
    public boolean storeObject(String sFile, DataObject fobj){
        try{
            Path path = Paths.get(sFile);
            String mime = Files.probeContentType(path);
            if(!Utils.isNullOrEmpty(mime)){
                fobj.setMimeType(mime);
            }

            try(FileInputStream fis = new FileInputStream(sFile)){
                return storeObject(fis, fobj);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 存储二进制数组
     * @param buff 数据数组
     * @param fobj 数据对象
     * @return true=执行成功
     */
    public boolean storeObject(byte[] buff, DataObject fobj){
        return storeObject(buff, 0, buff.length, fobj);
    }

    /**
     * 存储二进制数组
     * @param buff 数据数组
     * @param off 数据偏移
     * @param len 数据长度
     * @param fobj 数据对象
     * @return true=执行成功
     */
    public boolean storeObject(byte[] buff, int off, int len, DataObject fobj){
        try{
            try(ByteArrayInputStream dms = new ByteArrayInputStream(buff, off, len)){
                return storeObject(dms, fobj);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 保存二进制流
     * @param ins 输入流
     * @param fobj 数据对象
     * @return true=执行成功
     */
    public boolean storeObject(InputStream ins, DataObject fobj){
        try {
            if(!DataStoreHelper.newDoidOfObject(fobj)){
                return false;
            }
            if(!DataStoreHelper.newUuidOfObject(fobj)) {
                return false;
            }

            DataNodeRouter router = DataServers.getInstance();
            DataServer server = router.getServer(fobj.getRid(), DataConst.DataType_Object, fobj.getDoid(), fobj.getOid(), true, fobj);
            if(server.objectExists(fobj.getOid(), fobj.getDoid())!=null){
                /**
                 * 数据对象已存在，直接返回
                 */
                return true;
            }
            if(fobj.getSize()==0){
                /**
                 * 数据长度为0，直接添加数据对象
                 */
                server.addObject(fobj);
                return true;
            }

            /**
             * 缓存数据块ID串
             */
            StringBuilder str = new StringBuilder(8192);
            tLocals.put(fobj.getDoid(), str);

            if(hashProducer.computeHashBytesByBlock(ins, fobj.getBlobSize(), "SHA-256", fobj)==null){
                DataStoreHelper.onStoreFailed(fobj);
                return false;
            }
            return true;
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 通知完成数据块的HASH校验
     * @param data 数据块
     * @param off 数据偏移
     * @param len 数据长度
     * @param hashObj 哈希码，byte[] 或 String
     * @param isByteArr true=哈希码为byte[], false=哈希码为String
     * @param userData 自定义回调数据
     * @return true=继续执行 false=中断返回
     */
    public boolean onHashBlock(byte[] data, int off, int len, Object hashObj, boolean isByteArr, Object userData){
        String boid, hashStr;
        if(isByteArr) {
            byte[] idArr = (byte[])hashObj;
            hashStr = Utils.byteArrayToHexString(idArr, 0, idArr.length);
            boid = HashHelper.getShortHashStr(idArr, 12);
        } else {
            hashStr = (String)hashObj;
            boid = HashHelper.getShortHashStr(hashStr, 12);
        }

        DataObject fobj = (DataObject)userData;
        StringBuilder str = (StringBuilder)tLocals.get(fobj.getDoid());
        str.append(boid + ";");

        boolean ret = DataStoreHelper.saveBlob(boid, fobj.getFlags(), hashStr, data, off, len, fobj);
        if(ret){
            DataBlobRef item = new DataBlobRef();
            item.setUuid(fobj.getUuid());
            item.setBoid(boid);
            item.setHash(hashStr);
            item.setSize(len);
            ret = DataStoreHelper.addBlobIds(fobj.getRid(), fobj.getUid(), item);
        }
        return ret;
    }

    /**
     * 通知哈希码计算完成
     * @param hashObj 哈希码，byte[] 或 String
     * @param isByteArr true=哈希码为byte[], false=哈希码为String
     * @param userData 自定义回调数据
     * @return true=继续执行 false=中断返回
     */
    public boolean onHashComplete(Object hashObj, boolean isByteArr, Object userData){
        String hashStr;
        if(isByteArr) {
            byte[] idArr = (byte[])hashObj;
            hashStr = Utils.byteArrayToHexString(idArr, 0, idArr.length);
        } else {
            hashStr = (String)hashObj;
        }

        DataObject fobj = (DataObject)userData;
        fobj.setHash(hashStr);

        StringBuilder str = (StringBuilder)tLocals.get(fobj.getDoid());
        tLocals.remove(fobj.getDoid());
        return DataStoreHelper.saveObject(fobj);
    }
}
