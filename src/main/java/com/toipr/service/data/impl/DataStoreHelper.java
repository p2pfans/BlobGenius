package com.toipr.service.data.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataBlobIds;
import com.toipr.model.data.DataConst;
import com.toipr.model.data.DataObject;
import com.toipr.service.server.DataNodeRouter;
import com.toipr.service.server.DataServer;
import com.toipr.service.server.DataServers;
import com.toipr.util.HashHelper;

import java.util.Date;
import java.util.List;

public class DataStoreHelper {
    /**
     * 生成数字对象ID
     * @param fobj
     * @return
     */
    public static boolean newDoidOfObject(DataObject fobj){
        String sValue;
        if(fobj.getPid()!=null){
            sValue = String.format("%s-%s-%s-%s", fobj.getName(), fobj.getUid(), fobj.getRid(), fobj.getPid());
        } else {
            sValue = String.format("%s-%s-%s-", fobj.getName(), fobj.getUid(), fobj.getRid());
        }

        try {
            String idstr = HashHelper.computeHash(sValue.getBytes(), "SHA-256");
            idstr = HashHelper.getShortHashStr(idstr, 12);
            fobj.setDoid(idstr);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean newUuidOfObject(DataObject fobj){
        String sValue;
        if(fobj.getTag()!=null){
            sValue = String.format("%s-%s-%08d", fobj.getDoid(), fobj.getTag(), fobj.getVersion());
        } else {
            sValue = String.format("%s-%s-%08d", fobj.getDoid(), "master", fobj.getVersion());
        }

        try {
            String idstr = HashHelper.computeHash(sValue.getBytes(), "SHA-256");
            idstr = HashHelper.getShortHashStr(idstr, 12);
            fobj.setUuid(idstr);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean addBlobIds(String rid, String oid, DataBlobIds item){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_BlobIds, item.getUuid(), null, true, null);
        if(server==null){
            return false;
        }
        return server.addBlobIds(item)>0;
    }

    /**
     * 存储数据块
     * @param boid 数据块ID
     * @param flags 数据块标志
     * @param hashStr 文件块校验码
     * @param data 数据数组
     * @param off 数据起始位置
     * @param len 数据长度
     * @param fobj 数字对象实例
     * @return 成功=true 失败=false
     */
    public static boolean saveBlob(String boid, int flags, String hashStr, byte[] data, int off, int len, DataObject fobj){
        DataNodeRouter router = DataServers.getInstance();
        /**
         * 根据数据类型与数据块ID分配存储结点
         */
        DataServer server = router.getServer(fobj.getRid(), DataConst.DataType_Blob, boid, fobj.getOid(), true, fobj);
        if(server==null){
            return false;
        }

        Object rid = server.blobExists(boid);
        if(rid!=null){
            /**
             * 发现有相同数据块，增加一次引用计数
             */
            return (server.incBlobRefs(boid) > 0);
        }

        DataBlob blob = new DataBlob();
        blob.setBoid(boid);
        blob.setFlags((short)flags);
        //#0001 发现一次BUG，导致最后一块数据处理有问题
        if(off==0 && len==data.length) {
            blob.setData(data);
            blob.setSize(len);
        } else {
            int len2 = len - off;
            /**
             * 至少分配256字节，处理小碎片问题
             */
            byte[] data2 = new byte[len2<256 ? 256:len2];
            for(int i=0, j=0; i<len2; i++, j++){
                data2[i] = data[off + j];
            }
            blob.setData(data2);
            blob.setSize(len2);
        }
        blob.setCopy(1);
        blob.setRefs(1);//初始引用计数为1
        blob.setHash(hashStr);
        blob.setDownload(0);
        blob.setLastAccess(new Date());
        blob.setTimeCreate(new Date());
        return (server.addBlob(blob) > 0);
    }

    /**
     * 存储数字对象
     * @param fobj 数字对象实例
     * @return 成功=true 失败=false
     */
    public static boolean saveObject(DataObject fobj){
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(fobj.getRid(), DataConst.DataType_Object, fobj.getUuid(), fobj.getOid(), true, fobj);
        if(server==null){
            return false;
        }

        Object rid = server.objectExists(fobj.getOid(), fobj.getUuid());
        if(rid!=null) {
            return true;
        }

        fobj.setLastAccess(new Date());
        fobj.setTimeCreate(new Date());
        fobj.setLastModify(new Date());
        return (server.addObject(fobj) > 0);
    }

    /**
     * 减少所有已存储数据库块引用
     * @param rid 资源ID
     * @param oid 拥有者ID
     * @param uuid 数字对象ID
     * @param fobj 数字对象实例，可选
     */
    public static void decAllBlobRefs(String rid, String oid, String uuid, DataObject fobj){
        /**
         * 只是减少引用计数，引用计数=0的数据块，后续有程序负责定时清除
         * 优化I/O性能的一种有效手段，避免短时重传时减少写操作
         */
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(rid, DataConst.DataType_BlobIds, uuid, oid, true, fobj);
        if(server==null){
            return;
        }

        List<DataBlobIds> plist = server.getBlobIds(uuid);
        if(plist==null || plist.size()==0){
            return;
        }

        for(DataBlobIds item:plist){
            server = router.getServer(rid, DataConst.DataType_Blob, item.getBoid(), oid, true, fobj);
            if(server!=null){
                server.decBlobRefs(item.getBoid());
            }
        }
    }

    /**
     * 数字对象存储失败回调
     * @param fobj 数字对象实例
     */
    public static void onStoreFailed(DataObject fobj){
        /**
         * 存储失败，数据块引用减-1
         */
        decAllBlobRefs(fobj.getRid(), fobj.getOid(), fobj.getUuid(), fobj);

        /**
         * 清除文件块ID串对象
         */
        DataNodeRouter router = DataServers.getInstance();
        DataServer server = router.getServer(fobj.getRid(), DataConst.DataType_BlobIds, fobj.getUuid(), fobj.getOid(), true, fobj);
        if(server!=null){
            server.removeBlobIds(fobj.getUuid());
        }
    }
}
