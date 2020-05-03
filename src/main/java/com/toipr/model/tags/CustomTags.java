package com.toipr.model.tags;

import java.util.Date;

/**
 * 自定义标签
 */
public class CustomTags {
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
     * 父标签ID
     */
    private int pid;
    public int getPid(){
        return this.pid;
    }
    public void setPid(int pid){
        this.pid = pid;
    }

    /**
     * 标签类型
     */
    private String type;
    public String getType(){
        return this.type;
    }
    public void setType(String type){
        this.type = type;
    }

    /**
     * 标签键
     */
    private String key;
    public String getKey(){
        return this.key;
    }
    public void setKey(String key){
        this.key = key;
    }

    /**
     * 标签类型
     */
    private String value;
    public String getValue(){
        return this.value;
    }
    public void setValue(String value){
        this.value = value;
    }

    /**
     * 键类型，如int, string
     */
    private String keyType;
    public String getKeyType(){
        return this.keyType;
    }
    public void setKeyType(String keyType){
        this.keyType = keyType;
    }

    /**
     * 值类型，如int,string,list等
     */
    private String valType;
    public String getValType(){
        return this.valType;
    }
    public void setValType(String valType){
        this.valType = valType;
    }

    /**
     * 集合元素类型
     */
    private String elemType;
    public String getElemType(){
        return this.elemType;
    }
    public void setElemType(String elemType){
        this.elemType = elemType;
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
