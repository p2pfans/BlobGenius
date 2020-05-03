package com.toipr.model.node;

import java.util.Date;

public class CacheNode {
    /**
     * 记录ID
     */
    private int id;
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }

    /**
     * 服务器状态
     */
    private int state;
    public int getState(){
        return this.state;
    }
    public void setState(int state){
        this.state = state;
    }

    /**
     * 主机地址,格式：地址+端口，如localhost:6379
     */
    private String host;
    public String getHost(){
        return this.host;
    }
    public void setHost(String host){
        this.host = host;
    }

    /**
     * 数据库名, 方便配置管理
     */
    private String name;
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    /**
     * 是否为主服务器
     */
    private boolean master;
    public boolean getMaster(){
        return this.master;
    }
    public void setMaster(boolean master){
        this.master = master;
    }

    /**
     * redis数据库序号
     */
    private int dbIndex;
    public int getDbIndex(){
        return this.dbIndex;
    }
    public void setDbIndex(int dbIndex){
        this.dbIndex = dbIndex;
    }

    /**
     * 连接密码
     */
    private String dbPass;
    public String getDbPass(){
        return this.dbPass;
    }
    public void setDbPass(String dbPass){
        this.dbPass = dbPass;
    }

    /**
     * 节点负责存储数据的主键映射规则
     * Key生成规则由DataRule的ruleHost定义
     * 格式：范围型=[开始-结束],如30-4f; 离散型=[v1, v2, v3, ...], 如90,a0,b0etc
     *      允许多值，多个主键用'|'分隔，如30-4f|90,a0,b0
     * 例如：distKey抽取0-F等16个主键，自己负责0-8的部分，另一个节点负责9-F
     */
    private String distKey;
    public String getDistKey(){
        return this.distKey;
    }
    public void setDistKey(String distKey){
        this.distKey = distKey;
    }

    /**
     * 节点映射生成规则，多个规则用';;'分开，实现同一类型数据分服务器存取
     * 格式：字段名=方法:参数，方法目前支持substr，后续可优化
     * 如doid=substr:3-1;;oid=substr:3-1,
     * 从doid字段的第3个字符开始取1个字符、从oid字段第3个字符取1个字符，两字符串连接
     */
    private String distRule;
    public String getDistRule(){
        return this.distRule;
    }
    public void setDistRule(String distRule){
        this.distRule = distRule;
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
     * 创建时间戳
     */
    private Date timeCreate;
    public Date getTimeCreate(){
        return this.timeCreate;
    }
    public void setTimeCreate(Date timeCreate){
        this.timeCreate = timeCreate;
    }

    /**
     * 最近访问时间戳
     */
    private Date lastAccess;
    public Date getLastAccess(){
        return this.lastAccess;
    }
    public void setLastAccess(Date lastAccess){
        this.lastAccess = lastAccess;
    }
}
