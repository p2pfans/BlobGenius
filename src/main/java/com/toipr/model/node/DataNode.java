package com.toipr.model.node;

import java.util.Date;

/**
 * 数据存储结点，配置数据对象分服务器映射规则
 */
public class DataNode {
    /**
     * 正常状态，默认
     */
    public static final int state_normal = 0;
    /**
     * 服务器在线，有心跳报送
     */
    public static final int state_online = 1;
    /**
     * 数据维护，暂停服务
     */
    public static final int state_maintain = 3;

    /**
     * 服务器故障
     */
    public static final int state_error = 5;
    /**
     * 服务器停止
     */
    public static final int state_stopped = 7;

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
     * 主机唯一代码
     */
    private String hid;
    public String getHid(){
        return this.hid;
    }
    public void setHid(String hid){
        this.hid = hid;
    }

    /**
     * 主机编号，格式：国家缩写/6位邮编/机房代码-4位机架编号-主机编号
     */
    private String hostid;
    public String getHostid(){
        return this.hostid;
    }
    public void setHostid(String hostid){
        this.hostid = hostid;
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
     * 主机地址或域名+端口,格式：host:port
     */
    private String host;
    public String getHost(){
        return this.host;
    }
    public void setHost(String host){
        this.host = host;
    }

    /**
     * 传输协议, 默认jdbc，如http,https,rmi等
     */
    private String protocol = "jdbc";
    public String getProtocol(){
        return this.protocol;
    }
    public void setProtocol(String protocol){
        this.protocol = protocol;
    }

    /**
     * 数据库类型，如mysql
     */
    private String dbType;
    public String getDbType(){
        return this.dbType;
    }
    public void setDbType(String dbType){
        this.dbType = dbType;
    }

    /**
     * 资源代码
     */
    private String rid;
    public String getRid(){
        return this.rid;
    }
    public void setRid(String rid){
        this.rid = rid;
    }

    /**
     * 数据类型，如objects, blobs, blobIds等，根据需求自定义
     */
    private String dataType;
    public String getDataType(){
        return this.dataType;
    }
    public void setDataType(String dataType){
        this.dataType = dataType;
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
     * 本地文件路径, 此配置仅对DataBlob管用,path非空，数据块存储到文件系统
     * 解决问题1：数据块数据写入文件系统，块属性写入数据库，实现二者分离
     * 解决问题2：数据库备份到指定文件目录
     */
    private String filePath;
    public String getFilePath(){
        return this.filePath;
    }
    public void setFilePath(String path){
        this.filePath = path;
    }

    /**
     * 文件服务器地址, IP+port，端口默认35168
     */
    private String fileHost;
    public String getFileHost(){
        return this.fileHost;
    }
    public void setFileHost(String fileHost){
        this.fileHost = fileHost;
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
     * 链接用户名
     */
    private String dbUser;
    public String getDbUser(){
        return this.dbUser;
    }
    public void setDbUser(String dbUser){
        this.dbUser = dbUser;
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
     * 是否使用Unicode
     */
    private int useUnicode;
    public int getUseUnicode(){
        return this.useUnicode;
    }
    public void setUseUnicode(int useUnicode){
        this.useUnicode = useUnicode;
    }

    /**
     * 服务器时区
     */
    private String timeZone;
    public String getTimeZone(){
        return this.timeZone;
    }
    public void setTimeZone(String timeZone){
        this.timeZone = timeZone;
    }

    /**
     * 数据库驱动类
     */
    private String driverClass;
    public String getDriverClass(){
        return this.driverClass;
    }
    public void setDriverClass(String driverClass){
        this.driverClass = driverClass;
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
    private Date tmCreate;
    public Date getTmCreate(){
        return this.tmCreate;
    }
    public void setTmCreate(Date tmCreate){
        this.tmCreate = tmCreate;
    }

    /**
     * 最近存活汇报时间戳
     */
    private Date lastAlive;
    public Date getLastAlive(){
        return this.lastAlive;
    }
    public void setLastAlive(Date lastAlive){
        this.lastAlive = lastAlive;
    }
}
