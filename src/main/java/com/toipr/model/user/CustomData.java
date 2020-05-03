package com.toipr.model.user;

/**
 * 用户或机构等对象，关联自定义数据
 */
public class CustomData {
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
     * 对象ID
     */
    private String oid;
    public String getOid(){
        return this.oid;
    }
    public void setUid(String oid){
        this.oid = oid;
    }

    /**
     * 属性名称, 如：email, phone, wechat，weibo等
     */
    private String field;
    public String getField(){
        return this.field;
    }
    public void setField(String field){
        this.field = field;
    }

    /**
     * 属性值
     */
    private String value;
    public String getValue(){
        return this.value;
    }
    public void setValue(String value){
        this.value = value;
    }
}
