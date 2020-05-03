package com.toipr.model.conf;

public class SysConf {
    /**
     * 记录ID，自增长
     */
    private int id = 0;
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }

    /**
     * 配置名称
     */
    private String name;
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    /**
     * 数据类型，支持原始类型
     */
    private String type;
    public String getType(){
        return this.type;
    }
    public void setType(String type){
        this.type = type;
    }

    /**
     * 配置值
     */
    private String value;
    public String getValue(){
        return this.value;
    }
    public void setValue(String value){
        this.value = value;
    }
}
