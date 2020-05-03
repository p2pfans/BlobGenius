package com.toipr.model.oacl;

import java.util.Date;

/**
 * 加入用户组的用户
 */
public class ObjectAccessGroupUser {
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
     * 是否组管理员 true=管理员
     */
    private boolean isOwner;
    public boolean getIsOwner(){
        return this.isOwner;
    }
    public void setIsOwner(boolean isOwner){
        this.isOwner = isOwner;
    }

    /**
     * 组内角色, 自定义标签ID
     */
    private int role;
    public int getRole(){
        return this.role;
    }
    public void setRole(int role){
        this.role = role;
    }

    /**
     * 授权人操作IP地址
     */
    private String ipAddr;
    public String getIpAddr(){
        return this.ipAddr;
    }
    public void setIpAddr(String ipAddr){
        this.ipAddr = ipAddr;
    }

    /**
     * 授权人ID
     */
    private String uidGrant;
    public String getUidGrant(){
        return this.uidGrant;
    }
    public void setUidGrant(String uidGrant){
        this.uidGrant = uidGrant;
    }

    /**
     * 最后授权时间
     */
    private Date timeGrant;
    public Date getTimeGrant(){
        return this.timeGrant;
    }
    public void setTimeGrant(Date timeGrant){
        this.timeGrant = timeGrant;
    }
}
