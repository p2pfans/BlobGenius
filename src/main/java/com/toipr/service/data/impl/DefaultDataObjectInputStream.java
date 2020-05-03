package com.toipr.service.data.impl;

import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataConst;
import com.toipr.model.data.DataObject;
import com.toipr.model.data.DataBlobIds;
import com.toipr.service.server.DataNodeRouter;
import com.toipr.service.server.DataServer;
import com.toipr.service.server.DataServers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class DefaultDataObjectInputStream extends InputStream {
    /**
     * 数据对象
     */
    protected DataObject dataObject;
    /**
     * 数据节点路由器
     */
    protected DataNodeRouter nodeRouter;

    /**
     * 数据块数组
     */
    protected byte[] buffer = null;
    /**
     * 数据块长度
     */
    protected int dataLen = 0;
    /**
     * 数据块的当前偏移
     */
    protected int bufIndex = 0;
    /**
     * 数据对象的当前偏移
     */
    protected int pointer = 0;

    /**
     * 当前数据块编号，从0开始
     */
    protected int blobIndex = 0;
    /**
     * 数据块ID数组
     */
    protected String[] blobIds;

    public DefaultDataObjectInputStream(DataObject dobj){
        this.dataObject = dobj;
        this.nodeRouter = DataServers.getInstance();
    }

    @Override
    public int read() throws IOException {
        if(buffer==null){
            if(pointer>=dataObject.getSize()){
                return -1;
            }
            adjustPointer(pointer);
            if(buffer==null){
                throw new IOException("end of data object");
            }
        }

        /**
         * 技巧：字节与0xff做位与运算，转换为无符号整数，否则会出现-1问题
         */
        int c = 0xff & buffer[bufIndex++];
        if(bufIndex==dataLen || bufIndex==buffer.length){
            buffer = null;
        }
        pointer++;
        return c;
    }

    @Override
    public int available() throws IOException {
        return dataObject.getSize();
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long temp = this.pointer + n;
        if(temp<0){
            throw new IOException("skip less than zero");
        }
        if(temp>=dataObject.getSize()){
            throw new IOException("skip over data size");
        }
        adjustPointer((int)temp);
        return n;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.pointer = 0;
        this.buffer = null;
        this.bufIndex = 0;
        adjustPointer(0);
    }

    @Override
    public synchronized  void close() throws IOException {
        this.blobIds = null;
        this.buffer = null;
        this.dataObject = null;
        this.nodeRouter = null;
    }

    protected synchronized  void adjustPointer(int offset) throws IOException {
        int blobSize = dataObject.getBlobSize();
        int index = offset / blobSize;
        if(blobIds==null){
            initBlobIds();
        }
        if(index>=blobIds.length){
            throw new IOException("offset over than object size");
        }

        String bid = blobIds[index];
        DataServer server = nodeRouter.getServer(dataObject.getRid(), DataConst.DataType_Blob, bid, dataObject.getOid(), false, dataObject);
        if(server==null){
            throw new IOException("blob server not found");
        }

        DataBlob blob = server.getBlob(bid);
        if(blob==null){
            throw new IOException("blob not found");
        }
        blobIndex = index;
        buffer = blob.getData();
        dataLen = blob.getSize();
        bufIndex = offset % dataObject.getBlobSize();
        server.incBlobDown(bid, new Date());
    }

    protected synchronized void initBlobIds() throws IOException {
        DataServer server = nodeRouter.getServer(dataObject.getRid(), DataConst.DataType_BlobIds, dataObject.getDoid(), dataObject.getOid(), false, dataObject);
        List<DataBlobIds> oids = server.getBlobIds(dataObject.getDoid());
        if (oids == null) {
            throw new IOException("blobids not found");
        }

        int index = 0;
        blobIds = new String[oids.size()];
        for(DataBlobIds temp:oids){
            blobIds[index++] = temp.getBoid();
        }
    }
}