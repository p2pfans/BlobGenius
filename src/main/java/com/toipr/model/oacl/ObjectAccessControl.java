package com.toipr.model.oacl;

import java.util.Date;

/**
 * 对象访问控制，资源与数据对象
 */
public class ObjectAccessControl {
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
     * 拥有者ID oid+doid联合索引
     */
    private String oid;
    public String getOid(){
        return this.oid;
    }
    public void setOid(String oid){
        this.oid = oid;
    }

    /**
     * 数字对象ID
     */
    private String doid;
    public String getDoid(){
        return this.doid;
    }
    public void setDoid(String rid){
        this.doid = doid;
    }

    /**
     * 是否资源对象 false=DataObject true=DataResource
     */
    private boolean isRes = false;
    public boolean getIsRes(){
        return this.isRes;
    }
    public void setIsRes(boolean isRes){
        this.isRes = isRes;
    }

    /**
     * 是否有读权限
     */
    private boolean canRead = false;
    public boolean getCanRead(){
        return this.canRead;
    }
    public void setCanRead(boolean canRead){
        this.canRead = canRead;
    }

    /**
     * 是否有修改权限
     */
    private boolean canModify = false;
    public boolean getCanModify(){
        return this.canModify;
    }
    public void setCanModify(boolean canModify){
        this.canModify = canModify;
    }

    /**
     * 是否有删除权限
     */
    private boolean canDelete = false;
    public boolean getCanDelete(){
        return this.canDelete;
    }
    public void setCanDelete(boolean canDelete){
        this.canDelete = canDelete;
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
