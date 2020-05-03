package com.toipr.model.oacl;

import java.util.Date;

/**
 * 用户组
 */
public class ObjectAccessGroup {
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
     * 用户组ID
     */
    private String gid;
    public String getGid(){
        return this.gid;
    }
    public void setGid(String gid){
        this.gid = gid;
    }

    /**
     * 用户组父ID，可以继承权限约定
     */
    private String pid;
    public String getPid(){
        return this.pid;
    }
    public void setPid(String pid){
        this.pid = pid;
    }

    /**
     * 组名称
     */
    private String name;
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    /**
     * 组描述
     */
    private String brief;
    public String getBrief(){
        return this.brief;
    }
    public void setBrief(String brief){
        this.brief = brief;
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
}
