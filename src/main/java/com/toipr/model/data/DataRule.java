package com.toipr.model.data;

/**
 * 数据节点存储映射规则
 */
public class DataRule {
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
     * 资源ID
     */
    private String rid;
    public String getRid(){
        return this.rid;
    }
    public void setRid(String rid){
        this.rid = rid;
    }

    /**
     * 拥有者ID
     */
    private String oid;
    public String getOid(){
        return this.oid;
    }
    public void setOid(String oid){
        this.oid = oid;
    }

    /**
     * 资源名称，可以是简单名称，也可以是com.toipr.images等包名形式
     */
    private String resource;
    public String getResource(){
        return this.resource;
    }
    public void setResource(String resource){
        this.resource = resource;
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
     * 数据库名称
     */
    private String dbName;
    public String getDbName(){
        return this.dbName;
    }
    public void setDbName(String dbName){
        this.dbName = dbName;
    }

    /**
     * 数据表名称或前缀
     */
    private String tblName;
    public String getTblName(){
        return this.tblName;
    }
    public void setTblName(String tblName){
        this.tblName = tblName;
    }

    /**
     * 分节点映射规则，多个规则用';;'分开，实现同一个服务器分表机制
     * 格式：字段名=方法:参数，方法目前支持substr，后续可优化
     * 如doid=substr:3-1;;oid=substr:3-1,
     * 从doid字段的第3个字符开始取1个字符、从oid字段第3个字符取1个字符，两字符串连接
     */
    private String ruleHost;
    public String getRuleHost(){
        return this.ruleHost;
    }
    public void setRuleHost(String ruleHost){
        this.ruleHost = ruleHost;
    }

    /**
     * 分表存储映射规则，格式参考distRuleHost
     */
    private String ruleTable;
    public String getRuleTable(){
        return this.ruleTable;
    }
    public void setRuleTable(String ruleTable){
        this.ruleTable = ruleTable;
    }

    /**
     * 数据表名称替换标签
     */
    private String tblTag = "{tableName}";
    public String getTblTag(){
        return this.tblTag;
    }
    public void setTblTag(String tblTag){
        this.tblTag = tblTag;
    }

    /**
     * 数据块最大副本数，默认不限制
     */
    private int maxCopy=0;
    public int getMaxCopy(){
        return this.maxCopy;
    }
    public void setMaxCopy(int maxCopy){
        this.maxCopy = maxCopy;
    }

    /**
     * 数据块尺寸，默认1MB
     */
    private int blobSize = 1048576;
    public int getBlobSize(){
        return this.blobSize;
    }
    public void setBlobSize(int blobSize){
        this.blobSize = blobSize;
    }

    /**
     * 数据库创建SQL语句
     */
    private String dbSql;
    public String getDbSql(){
        return this.dbSql;
    }
    public void setDbSql(String dbSql){
        this.dbSql = dbSql;
    }

    /**
     * 数据表创建SQL语句
     */
    private String tblSql;
    public String getTblSql(){
        return this.tblSql;
    }
    public void setTblSql(String tblSql){
        this.tblSql = tblSql;
    }
}
