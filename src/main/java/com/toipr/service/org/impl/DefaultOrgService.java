package com.toipr.service.org.impl;

import com.toipr.mapper.org.OrgInfoMapper;
import com.toipr.model.user.OrgInfo;
import com.toipr.service.DefaultService;
import com.toipr.service.org.OrgService;
import com.toipr.util.HashHelper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class DefaultOrgService extends DefaultService implements OrgService {
    public DefaultOrgService(ApplicationContext context){
        super(context);
    }

    /**
     * 添加机构对象
     * @param obj 机构对象
     * @return 成功=true 失败=false
     */
    public boolean addOrg(OrgInfo obj){
        String oid = getOid(obj.getName(), obj.getPid());
        if(oid==null){
            return false;
        }
        obj.setOid(oid);

        try(SqlSession session = mybatis.getSession()){
            OrgInfoMapper mapper = session.getMapper(OrgInfoMapper.class);
            int ret = mapper.addOrg(obj, "org_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置机构状态
     * @param oid 机构ID
     * @param state 机构状态
     * @return 成功=true 失败=false
     */
    public boolean setState(String oid, int state){
        try(SqlSession session = mybatis.getSession()){
            OrgInfoMapper mapper = session.getMapper(OrgInfoMapper.class);
            int ret = mapper.setState(oid, state, "org_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 删除机构对象
     * @param oid 机构ID
     * @return 成功=true 失败=false
     */
    public boolean removeOrg(String oid){
        try(SqlSession session = mybatis.getSession()){
            OrgInfoMapper mapper = session.getMapper(OrgInfoMapper.class);
            int ret = mapper.removeOrg(oid, "org_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 机构是否存在
     * @param name 机构名称
     * @param pid 父机构ID，可以为null
     * @return 存在=机构ID 不存在=null
     */
    public String orgExists(String name, String pid){
        try(SqlSession session = mybatis.getSession()) {
            OrgInfoMapper mapper = session.getMapper(OrgInfoMapper.class);
            return mapper.orgExists(name, pid, "org_info");
        }
    }

    /**
     * 获取机构对象oid
     * @param oid 机构ID
     * @return 机构对象实例
     */
    public OrgInfo getOrg(String oid){
        try(SqlSession session = mybatis.getSession()) {
            OrgInfoMapper mapper = session.getMapper(OrgInfoMapper.class);
            return mapper.getOrg(oid, "org_info");
        }
    }

    /**
     * 根据参数获取机构列表
     * @param params 参数映射表
     * @param start 开始记录
     * @param count 记录数量
     * @param olist 机构列表
     * @return 成功=记录数总数 失败=0
     */
    public int getOrgList(Map<String, Object> params, int start, int count, List<OrgInfo> olist){
        int total = 0;
        try(SqlSession session = mybatis.getSession()) {
            OrgInfoMapper mapper = session.getMapper(OrgInfoMapper.class);
            total = mapper.count(params, "org_info");
            if(total>0){
                List<OrgInfo> plist = mapper.getOrgList(params, start, count, "org_info");
                if(plist!=null){
                    olist.addAll(plist);
                }
            }
        }
        return total;
    }

    protected String getOid(String name, String pid){
        String idstr = String.format("%s-%s", pid, name);
        try{
            byte[] hashBytes = HashHelper.computeHashBytes(idstr.getBytes("utf-8"), "SHA-256");
            idstr = HashHelper.getShortHashStr(hashBytes, 8);
            return idstr;
        } catch(Exception ex){
            ;
        }
        return null;
    }
}
