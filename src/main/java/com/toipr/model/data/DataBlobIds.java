package com.toipr.model.data;

import java.util.Date;

/**
 * 数字对象的数据块ID串
 * 一个数据对象DataObject可以有若干个DataBlobIds对象
 */
public class DataBlobIds {
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
     * 数据对象ID
     */
    private String uuid;
    public String getUuid(){
        return this.uuid;
    }
    public void setUuid(String uuid){
        this.uuid = uuid;
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
     * 数据块在对象中的序号，偏移从0开始，解决流式写入与断点续传问题
     */
    private int serial = 0;
    public int getSerial(){
        return this.serial;
    }
    public void setSerial(int serial){
        this.serial = serial;
    }

    /**
     * 数据在数据块中的偏移，偏移从0开始，解决多个数据块合并问题
     * 解决问题：多个小尺寸数据块合并成一个大数据块，解决大数量的小对象存储难题
     */
    private int offset = 0;
    public int getOffset(){
        return this.offset;
    }
    public void setOffset(int offset){
        this.offset = offset;
    }

    /**
     * 块大小
     */
    private int size = 0;
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
     * 对象创建时间
     */
    private Date timeCreate;
    public Date getTimeCreate(){
        return this.timeCreate;
    }
    public void setTimeCreate(Date timeCreate){
        this.timeCreate = timeCreate;
    }
}
