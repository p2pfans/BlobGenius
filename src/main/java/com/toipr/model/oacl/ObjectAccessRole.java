package com.toipr.model.oacl;

import java.util.Date;

/**
 * 对象访问角色
 */
public class ObjectAccessRole {
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
