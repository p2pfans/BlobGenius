package com.toipr.model.data;

import java.util.Date;

/**
 * 数据资源对象
 * 每个资源都由一个机构拥有，默认有一个机构对象
 * 注意：用户不能拥有一个资源，机构管理员可以管理数据资源
 */
public class DataResource {
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
     * 对象状态
     */
    private int state;
    public int getState(){
        return this.state;
    }
    public void setState(int state){
        this.state = state;
    }

    /**
     * 资源ID
     */
    private String rid;
    public String getRid(){
        return this.rid;
    }
    public void setRid(String rid){
        this.rid = rid;
    }

    /**
     * 拥有者ID
     */
    private String oid;
    public String getOid(){
        return this.oid;
    }
    public void setOid(String oid){
        this.oid = oid;
    }

    /**
     * 资源内部名称，采用java package命名规则
     */
    private String code;
    public String getCode(){
        return this.code;
    }
    public void setCode(String code){
        this.code = code;
    }

    /**
     * 资源显示名称
     */
    private String name;
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    /**
     * 资源标签，多值，用半角分号隔开
     */
    private String tags;
    public String getTags(){
        return this.tags;
    }
    public void setTags(String tags){
        this.tags = tags;
    }

    /**
     * 资源简介
     */
    private String brief;
    public String getBrief(){
        return this.brief;
    }
    public void setBrief(String brief){
        this.brief = brief;
    }

    /**
     * 允许最大存储对象数量，默认20万个
     */
    private long maxCount = 200000;
    public long getMaxCount(){
        return this.maxCount;
    }
    public void setMaxCount(long maxCount){
        this.maxCount = maxCount;
    }

    /**
     * 允许最大存储规模，单位字节，默认20GB
     */
    private long maxSpace = 20*1024*1024*1024;
    public long getMaxSpace(){
        return this.maxSpace;
    }
    public void setMaxSpace(long maxSpace){
        this.maxSpace = maxSpace;
    }

    /**
     * 总访问次数
     */
    private long totalVisit;
    public long getTotalVisit(){
        return this.totalVisit;
    }
    public void setTotalVisit(long totalVisit){
        this.totalVisit = totalVisit;
    }

    /**
     * 对象总数
     */
    private long totalCount;
    public long getTotalCount(){
        return this.totalCount;
    }
    public void setTotalCount(long totalCount){
        this.totalCount = totalCount;
    }

    /**
     * 数据存储规模
     */
    private long totalSpace;
    public long getTotalSpace(){
        return this.totalSpace;
    }
    public void setTotalSpace(long totalSpace){
        this.totalSpace = totalSpace;
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
     * 创建用户
     */
    private String uidCreate;
    public String getUidCreate(){
        return this.uidCreate;
    }
    public void setUidCreate(String uidCreate){
        this.uidCreate = uidCreate;
    }

    /**
     * 创建时间
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
}
