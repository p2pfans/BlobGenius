package com.toipr.service.resource.impl;

import com.toipr.mapper.resource.DataRuleMapper;
import com.toipr.mapper.resource.DataResourceMapper;
import com.toipr.model.data.DataResource;
import com.toipr.model.data.DataRule;
import com.toipr.service.DefaultService;
import com.toipr.service.resource.ResourceService;
import com.toipr.service.resource.RuleService;
import com.toipr.util.HashHelper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DefaultResourceService extends DefaultService implements ResourceService, RuleService {
    public DefaultResourceService(ApplicationContext context){
        super(context);
    }

    /**
     * 添加数据资源
     * @param obj 资源对象
     * @return 成功=true 失败=false
     */
    public boolean addResource(DataResource obj){
        String rid = getRid(obj.getOid(), obj.getCode());
        if(rid==null){
            return false;
        }
        obj.setRid(rid);

        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            int ret = mapper.addResource(obj);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置主机状态
     * @param rid 主机ID
     * @param state 工作状态
     * @return true=成功 false=失败
     */
    public boolean setState(String rid, int state){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            int ret = mapper.setState(rid, state);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户oid资源code是否存在
     * @param oid 拥有者ID
     * @param code 资源内部代码，如java package命名方式
     * @return 成功=资源ID 失败=null
     */
    public String resourceExists(String oid, String code){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            return mapper.resourceExists(oid, code);
        }
    }

    /**
     * 删除用户oid的资源code
     * @param oid 用户ID
     * @param rid 资源ID
     * @return 成功=true 失败=false
     */
    public boolean removeResource(String oid, String rid){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            int ret = mapper.removeResource(rid);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取资源对象rid
     * @param rid 资源ID
     * @return 资源对象实例
     */
    public DataResource getResource(String rid){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            return mapper.getResource(rid);
        }
    }

    /**
     * 获取用户oid的资源code
     * @param oid 用户ID
     * @param code 资源代码
     * @return 资源对象实例
     */
    public DataResource getResource(String oid, String code){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            return mapper.getResource2(oid, code);
        }
    }

    /**
     * 获取用户oid的资源列表
     * @param params 查询条件参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 资源列表
     * @return 记录总数
     */
    public int getResourceList(Map<String, Object> params, int start, int count, List<DataResource> rlist){
        int total = 0;
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            total = mapper.countResource(params);
            if(total>0) {
                List<DataResource> plist = mapper.getAllResources(params, start, count);
                if (plist != null) {
                    rlist.addAll(plist);
                }
            }
        }
        return total;
    }

    /**
     * 设置资源条目数
     * @param rid 资源ID
     * @param totalCount 资源总条目数
     * @return 成功=true 失败=false
     */
    public boolean setTotalCount(String rid, long totalCount){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            int ret = mapper.setTotalCount(rid, totalCount);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置资源存储容量
     * @param rid 资源ID
     * @param totalSpace 资源存储容量，单位字节
     * @return 成功=true 失败=false
     */
    public boolean setTotalSpace(String rid, long totalSpace){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            int ret = mapper.setTotalSpace(rid, totalSpace);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 增加总访问次数，设置最后访问时间
     * @param rid 资源ID
     * @param delta 访问次数增量
     * @param lastAccess 最后访问时间
     * @return 成功=true 失败=false
     */
    public boolean incVisitAndLastAccess(String rid, int delta, Date lastAccess){
        try(SqlSession session = mybatis.getSession()){
            DataResourceMapper mapper = session.getMapper(DataResourceMapper.class);
            int ret = mapper.incVisitAndLastAccess(rid, delta, lastAccess);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    protected String getRid(String oid, String code){
        String idstr = String.format("%s-%s", oid, code);
        try{
            byte[] hashBytes = HashHelper.computeHashBytes(idstr.getBytes("utf-8"), "SHA-256");
            idstr = HashHelper.getShortHashStr(hashBytes, 8);
            return idstr;
        } catch(Exception ex){
            ;
        }
        return null;
    }

    /**
     * 添加数据节点映射规则
     * @param item 映射规则
     * @return 成功=true 失败=false
     */
    public boolean addRule(DataRule item){
        try(SqlSession session = mybatis.getSession()){
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            int ret = mapper.addRule(item);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 删除数据节点映射规则
     * @param rid 资源ID
     * @return 成功=true 失败=false
     */
    public boolean removeRule(String rid){
        try(SqlSession session = mybatis.getSession()){
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            int ret = mapper.removeRule(rid);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 判断规则是否存在
     * @param oid 拥有者ID(Owner ID)
     * @param resource 资源名称
     * @param dataType 数据类型
     * @return 成功=true 失败=false
     */
    public boolean ruleExists(String oid, String resource, String dataType){
        try(SqlSession session = mybatis.getSession()) {
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            return mapper.ruleExists(oid, resource, dataType) != null;
        }
    }

    /**
     * 设置节点映射规则
     * @param rid 资源ID
     * @param ruleHost 映射规则
     * @return 成功=true 失败=false
     */
    public boolean setRuleHost(String rid, String ruleHost){
        try(SqlSession session = mybatis.getSession()){
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            int ret = mapper.setRuleHost(rid, ruleHost);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置数据表映射规则
     * @param rid 资源ID
     * @param ruleTable 映射规则
     * @return 成功=true 失败=false
     */
    public boolean setRuleTable(String rid, String ruleTable){
        try(SqlSession session = mybatis.getSession()){
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            int ret = mapper.setRuleTable(rid, ruleTable);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置存储数据库名与数据表名
     * @param rid 资源ID
     * @param dbName 数据库名
     * @param tblName 数据表名
     * @return 成功=true 失败=false
     */
    public boolean setTable(String rid, String dbName, String tblName){
        try(SqlSession session = mybatis.getSession()){
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            int ret = mapper.setTable(rid, dbName, tblName);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置数据库或数据表创建标签
     * @param rid 资源ID
     * @param isTable true=数据表 false=数据库
     * @param sqlText SQL语句
     * @return 成功=true 失败=false
     */
    public boolean setTableSQL(String rid, boolean isTable, String sqlText){
        try(SqlSession session = mybatis.getSession()){
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            int ret = mapper.setTableSQL(rid, isTable, sqlText);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 根据资源名称与数据类型获取节点映射规则
     * @param rid 资源ID
     * @return 映射规则对象
     */
    public DataRule getRule(String rid){
        try(SqlSession session = mybatis.getSession()) {
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            return mapper.getRule(rid);
        }
    }

    /**
     * 获取数据节点映射规则列表
     * @param params 查询参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 规则对象列表
     * @return 符合条件的规则总数
     */
    public int getRuleList(Map<String, Object> params, int start, int count, List<DataRule> rlist){
        int total = 0;
        try(SqlSession session = mybatis.getSession()) {
            DataRuleMapper mapper = session.getMapper(DataRuleMapper.class);
            total = mapper.count(params);
            if(total>0){
                List<DataRule> plist = mapper.getRuleList(params, start, count);
                if(plist!=null){
                    rlist.addAll(plist);
                }
            }
        }
        return total;
    }
}
