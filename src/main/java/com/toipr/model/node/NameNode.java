package com.toipr.model.node;

import java.util.Date;

/**
 * 对象搜索节点, ElasticSearch节点
 */
public class NameNode {
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
     * 资源代码
     */
    private String resid;
    public String getResid(){
        return this.resid;
    }
    public void setResid(String resid){
        this.resid = resid;
    }

    /**
     * 数据集名称
     */
    private String dbName;
    public String getDbName(){
        return this.dbName;
    }
    public void setDbName(String dbName){
        this.dbName = dbName;
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
}
