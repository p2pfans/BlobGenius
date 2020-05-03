package com.toipr.model.data;

import com.toipr.util.Utils;

import java.io.*;
import java.util.Date;

/**
 * 数据块对象，大数据对象分割的数据块
 * 数据块尺寸：256KB 512KB 1MB 2MB 4MB
 */
public class DataBlob {
    public static final int DataBlob_Magic = 0x424c4f42;//BLOB

    /**
     * 记录ID，自增长
     */
    private int id = 0;
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }

    /**
     * 数据块ID
     */
    private String boid;
    public String getBoid(){
        return this.boid;
    }
    public void setBoid(String boid){
        this.boid = boid;
    }

    /**
     * 数据块标志，如加密标志，压缩标志等
     */
    private short flags;
    public short getFlags(){
        return this.flags;
    }
    public void setFlags(short flags){
        this.flags = flags;
    }

    /**
     * 数据块的第几次备份,主服务器第1次，复制服务器递增
     */
    private int copy = 1;
    public int getCopy(){
        return this.copy;
    }
    public void setCopy(int copy){
        this.copy = copy;
    }

    /**
     * 块大小
     */
    private int size;
    public int getSize(){
        return this.size;
    }
    public void setSize(int size){
        this.size = size;
    }

    /**
     * 数据校验码，默认算法SHA-256
     */
    private String hash;
    public String getHash(){
        return this.hash;
    }
    public void setHash(String hash){
        this.hash = hash;
    }

    /**
     * 引用次数，与一个数字对象关联记一次，删除文件时减少引用计数
     */
    private int refs;
    public int getRefs(){
        return this.refs;
    }
    public void setRefs(int refs){
        this.refs = refs;
    }

    /**
     * 下载次数
     */
    private long download;
    public long getDownload(){
        return this.download;
    }
    public void setDownload(long download){
        this.download = download;
    }

    /**
     * 对象创建时间
     */
    private Date timeCreate;
    public Date getTimeCreate(){
        return this.timeCreate;
    }
    public void setTimeCreate(Date timeCreate){
        this.timeCreate = timeCreate;
    }

    /**
     * 上次访问时间戳
     */
    private Date lastAccess;
    public Date getLastAccess(){
        return this.lastAccess;
    }
    public void setLastAccess(Date lastAccess){
        this.lastAccess = lastAccess;
    }

    /**
     * 文件数据
     */
    private byte[] data;
    public byte[] getData(){
        return this.data;
    }
    public void setData(byte[] data){
        this.data = data;
    }

    public static DataBlob readObject(InputStream ins) throws IOException {
        ObjectInputStream ois = null;
        if(ins instanceof  ObjectInputStream){
            ois = (ObjectInputStream)ins;
        } else {
            ois = new ObjectInputStream(ins);
        }

        int tag = ois.readInt();
        if(tag!=DataBlob_Magic){
            return null;
        }
        short flags = ois.readShort();

        short idLen = ois.readShort();
        if(idLen<8 || idLen>48){
            return null;
        }
        byte[] idArr = new byte[256];
        if(!Utils.readAll(idArr, idLen, ois)){
            return null;
        }
        String boid = new String(idArr, 0, idLen, "utf-8");

        int copy = ois.readInt();
        int refs = ois.readInt();
        long downTimes = ois.readLong();
        long lastAccess = ois.readLong();

        int size = ois.readInt();
        if(size<=0 || size>4*1024*1024){
            return null;
        }

        byte[] data = new byte[size];
        if(!Utils.readAll(data, size, ois)){
            return null;
        }

        DataBlob blob = new DataBlob();
        blob.setBoid(boid);
        blob.setFlags(flags);

        blob.setCopy(copy);
        blob.setRefs(refs);
        blob.setSize(size);

        blob.setDownload(downTimes);
        blob.setLastAccess(new Date(lastAccess));
        blob.setData(data);
        return blob;
    }

    public boolean writeObject(OutputStream out) throws IOException {
        ObjectOutputStream oos = null;
        if(out instanceof  ObjectOutputStream){
            oos = (ObjectOutputStream)out;
        } else {
            oos = new ObjectOutputStream(out);
        }

        oos.writeInt(DataBlob_Magic);
        oos.writeShort(flags);

        byte[] idArr = boid.getBytes("utf-8");
        oos.writeShort((short)idArr.length);
        oos.write(idArr);

        oos.writeInt(copy);
        oos.writeInt(refs);
        oos.writeLong(download);
        oos.writeLong(lastAccess.getTime());

        oos.writeInt(size);
        oos.write(data);
        oos.flush();
        return true;
    }
}
