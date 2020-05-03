package com.toipr.service.data.impl;

import com.toipr.model.data.DataObject;
import com.toipr.util.HashHelper;
import com.toipr.util.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class DefaultDataObjectOutputStream extends OutputStream {
    protected DataObject object;

    protected int totalSize = 0;

    protected Object hashObj = null;

    protected int pointer = 0;
    protected byte[] dataBuf = null;
    protected StringBuilder idstr = new StringBuilder(8192);

    public DefaultDataObjectOutputStream(DataObject object){
        this.object = object;
        if(object.getBlobSize()>object.getSize() && object.getSize()>0){
            this.dataBuf = new byte[(int)object.getSize()];
        } else {
            this.dataBuf = new byte[object.getBlobSize()];
        }
        hashObj = HashHelper.getHashObj("md5");
    }

    @Override
    public void write(int c) throws IOException{
        dataBuf[pointer++] = (byte)c;
        if(pointer==dataBuf.length){
            String bid;
            try {
                bid = "boi" + HashHelper.computeHash(dataBuf, "md5");
            } catch(Exception ex){
                throw new IOException("compute block hash failed");
            }
            if(!onHashBlock(dataBuf, 0, dataBuf.length, bid)){
                throw new IOException("data block save failed");
            }
            HashHelper.update(hashObj, dataBuf, 0, dataBuf.length);
            pointer = 0;
        }
        totalSize++;
    }

    /**
     * 通知完成数据块的HASH校验
     * @param data 数据块
     * @param off 数据偏移
     * @param len 数据长度
     * @param hashObj 哈希码，byte[] 或 String
     * @return true=继续执行 false=中断返回
     */
    public boolean onHashBlock(byte[] data, int off, int len, Object hashObj){
        String boid, hashStr;
        if(hashObj instanceof String) {
            hashStr = (String)hashObj;
            boid = "boi" + HashHelper.getShortHashStr(hashStr, 12);
        } else {
            byte[] idArr = (byte[])hashObj;
            hashStr = Utils.byteArrayToHexString(idArr, 0, idArr.length);
            boid = "boi" + HashHelper.getShortHashStr(idArr, 12);
        }
        idstr.append(boid + ";");
        return DataStoreHelper.saveBlob(boid, object.getFlags(), hashStr, data, off, len, this.object);
    }

    /**
     * 通知哈希码计算完成
     * @param hashObj 哈希码，byte[] 或 String
     * @return true=继续执行 false=中断返回
     */
    public boolean onHashComplete(Object hashObj){
        DataObject fobj = this.object;
        fobj.setHash((String)hashObj);
        return DataStoreHelper.saveObject(fobj);
    }

    @Override
    public void flush() throws IOException {
        if(pointer>0){
            String boid;
            try {
                byte[] hashBytes = HashHelper.computeHashBytes(dataBuf, 0, pointer, "SHA-256");
                boid = "boi" + HashHelper.getShortHashStr(hashBytes, 12);
            } catch(Exception ex){
                throw new IOException("compute block hash failed");
            }
            if(!onHashBlock(dataBuf, 0, pointer, boid)){
                throw new IOException("data block save failed");
            }
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        if(object.getSize()>0 && object.getSize()>totalSize){
            throw new IOException("incomplete data object");
        }
        if(object.getSize()==0){
            object.setSize(totalSize);
        }
        String idHash = HashHelper.finalHash(hashObj);
        if(!onHashComplete(idHash)){
            DataStoreHelper.onStoreFailed(object);
            throw new IOException("data block save failed");
        }

        idstr = null;
        object = null;
        dataBuf = null;
        hashObj = null;
        pointer = 0;
    }
}
