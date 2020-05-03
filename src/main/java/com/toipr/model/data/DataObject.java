package com.toipr.model.data;

import java.util.Date;

/**
 * 数字对象，支持版本管理与分支管理
 */
public class DataObject {
    /**
     * 记录ID，自增长
     */
    private int id;
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }

    /**
     * 唯一标识，doid+tag+version计算
     * 支持版本管理、分支管理与多次提交
     */
    private String uuid;
    public String getUuid(){
        return this.uuid;
    }
    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    /**
     * 数字对象ID，不同版本与标签使用的ID相同
     */
    private String doid;
    public String getDoid(){
        return this.doid;
    }
    public void setDoid(String doid){
        this.doid = doid;
    }

    /**
     * 对象状态，如禁用等状态
     */
    private int state;
    public int getState(){
        return this.state;
    }
    public void setState(int state){
        this.state = state;
    }

    /**
     * 数据对象标志，如是否目录、是否加密，是否压缩、是否编码等
     */
    private int flags;
    public int getFlags(){
        return this.flags;
    }
    public void setFlags(int flags){
        this.flags = flags;
    }

    public boolean isDirectory(){
        return (flags&DataConst.DataFlags_Directory)!=0;
    }
    public boolean isCompressed(){
        return(flags&DataConst.DataFlags_Compress)!=0;
    }
    public boolean isCiphered(){
        return (flags&DataConst.DataFlags_Cipher)!=0;
    }
    public boolean isEncoded(){
        return (flags&DataConst.DataFlags_Encode)!=0;
    }

    /**
     * 机构ID
     */
    private String oid;
    public String getOid(){
        return this.oid;
    }
    public void setOid(String oid){
        this.oid = oid;
    }

    /**
     * 用户ID
     */
    private String uid;
    public String getUid(){
        return this.uid;
    }
    public void setUid(String uid){
        this.uid = uid;
    }

    /**
     * 父对象ID
     */
    private String pid;
    public String getPid(){
        return this.pid;
    }
    public void setPid(String pid){
        this.pid = pid;
    }

    /**
     * 资源对象ID
     */
    private String rid;
    public String getRid(){
        return this.rid;
    }
    public void setRid(String rid){
        this.rid = rid;
    }

    /**
     * 对象名称
     */
    private String name;
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    /**
     * 存储路径，包括对象名称
     */
    private String path;
    public String getPath(){
        return this.path;
    }
    public void setPath(String path){
        this.path = path;
    }

    /**
     * 对象数据长度
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
     * 数据块尺寸 默认1MB
     */
    private int blobSize = 1024 * 1024;
    public int getBlobSize(){
        return this.blobSize;
    }
    public void setBlobSize(int blobSize){
        this.blobSize = blobSize;
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
     * MimeType
     */
    private String mimeType;
    public String getMimeType(){
        if(mimeType==null || mimeType.length()==0){
            if(name!=null){
                int pos = name.lastIndexOf('.');
                if(pos>0){
                    mimeType = name.substring(pos+1);
                }
            }
        }
        return this.mimeType;
    }
    public void setMimeType(String mimeType){
        if(mimeType.length()>60){
            int pos = name.lastIndexOf('.');
            if(pos>0){
                mimeType = name.substring(pos+1);
            } else {
                mimeType = "unknown";
            }
        }
        this.mimeType = mimeType;
    }

    /**
     * 对象版本, 提交修改次数+1, 默认为1
     */
    private int version = 1;
    public int getVersion(){
        return this.version;
    }
    public void setVersion(int version){
        this.version = version;
    }

    /**
     * 版本控制，Fork/Tag，分支管理与版本管理标记，默认主分支master
     */
    private String tag = "master";
    public String getTag(){
        return this.tag;
    }
    public void setTag(String tag){
        this.tag = tag;
    }

    /**
     * 创建IP地址
     */
    private String ipAddr;
    public String getIpAddr(){
        return this.ipAddr;
    }
    public void setIpAddr(String ipAddr){
        this.ipAddr = ipAddr;
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
     * 最近访问时间
     */
    private Date lastAccess;
    public Date getLastAccess(){
        return this.lastAccess;
    }
    public void setLastAccess(Date lastAccess){
        this.lastAccess = lastAccess;
    }

    /**
     * 最近修改时间
     */
    private Date lastModify;
    public Date getLastModify(){
        return this.lastModify;
    }
    public void setLastModify(Date lastModify){
        this.lastModify = lastModify;
    }
}
