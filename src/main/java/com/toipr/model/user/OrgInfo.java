package com.toipr.model.user;

import java.util.Date;

/**
 * 机构信息
 */
public class OrgInfo {
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
     * 机构状态
     */
    private int state;
    public int getState(){
        return this.state;
    }
    public void setState(int state){
        this.state = state;
    }

    /**
     * 机构等级,如VIP等级
     */
    private int level;
    public int getLevel(){
        return this.level;
    }
    public void setLevel(int level){
        this.level = level;
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
     * 父机构ID
     */
    private String pid;
    public String getPid(){
        return this.pid;
    }
    public void setPid(String pid){
        this.pid = pid;
    }

    /**
     * 机构名称
     */
    private String name;
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    /**
     * 名称缩写
     */
    private String abbr;
    public String getAbbr(){
        return this.abbr;
    }
    public void setAbbr(String abbr){
        this.abbr = abbr;
    }

    /**
     * 联系人
     */
    private String contact;
    public String getContact(){
        return this.contact;
    }
    public void setContact(String contact){
        this.contact = contact;
    }

    /**
     * 担任职务
     */
    private String title;
    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * 电子邮箱
     */
    private String email;
    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * 联系电话
     */
    private String phone;
    public String getPhone(){
        return this.phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }

    /**
     * 机构标志logo
     */
    private String logo;
    public String getLogo(){
        return this.logo;
    }
    public void setLogo(String logo){
        this.logo = logo;
    }

    /**
     * 官方网站
     */
    private String website;
    public String getWebsite(){
        return this.website;
    }
    public void setWebsite(String website){
        this.website = website;
    }

    /**
     * 管理员账号
     */
    private String uidAdmin;
    public String getUidAdmin(){
        return this.uidAdmin;
    }
    public void setUidAdmin(String uidAdmin){
        this.uidAdmin = uidAdmin;
    }

    /**
     * 允许最大存储对象数量，默认2万个
     */
    private long maxCount = 20000;
    public long getMaxCount(){
        return this.maxCount;
    }
    public void setMaxCount(long maxCount){
        this.maxCount = maxCount;
    }

    /**
     * 允许最大存储规模，单位字节，默认5GB
     */
    private long maxSpace = 5*1024*1024*1024;
    public long getMaxSpace(){
        return this.maxSpace;
    }
    public void setMaxSpace(long maxSpace){
        this.maxSpace = maxSpace;
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
}
