package com.toipr.model.user;

import java.util.Date;

/**
 * 用户对象
 */
public class UserInfo {
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
     * 用户级别,如VIP等级
     */
    private int level;
    public int getLevel(){
        return this.level;
    }
    public void setLevel(int level){
        this.level = level;
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
     * 用户账号
     */
    private String username;
    public String getUsername(){
        return this.username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * 用户密码
     */
    private String password;
    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }

    /**
     * 用户昵称
     */
    private String nickname;
    public String getNickname(){
        return this.nickname;
    }
    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    /**
     * 性别 0=female 1=male 2=中性 3=未知
     */
    private int sex=3;
    public int getSex(){
        return this.sex;
    }
    public void setSex(int sex){
        this.sex = sex;
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
     * 机构名称
     */
    private String org;
    public String getOrg(){
        return this.org;
    }
    public void setOrg(String org){
        this.org = org;
    }

    /**
     * 职务
     */
    private String title;
    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * 电话
     */
    private String phone;
    public String getPhone(){
        return this.phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }

    /**
     * 邮箱
     */
    private String email;
    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * 允许最大存储对象数量，默认5000个
     */
    private long maxCount = 5000;
    public long getMaxCount(){
        return this.maxCount;
    }
    public void setMaxCount(long maxCount){
        this.maxCount = maxCount;
    }

    /**
     * 允许最大存储规模，单位字节，默认2GB
     */
    private long maxSpace = 2*1024*1024*1024;
    public long getMaxSpace(){
        return this.maxSpace;
    }
    public void setMaxSpace(long maxSpace){
        this.maxSpace = maxSpace;
    }

    /**
     * 总登录次数
     */
    private int totalLogin;
    public int getTotalLogin(){
        return this.totalLogin;
    }
    public void setTotalLogin(int totalLogin){
        this.totalLogin = totalLogin;
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

    /**
     * 最近登录IP地址
     */
    private String lastIpAddr;
    public String getLastIpAddr(){
        return this.lastIpAddr;
    }
    public void setLastIpAddr(String lastIpAddr){
        this.lastIpAddr = lastIpAddr;
    }
}
