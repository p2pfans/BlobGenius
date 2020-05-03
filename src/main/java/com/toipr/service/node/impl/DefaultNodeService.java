package com.toipr.service.node.impl;

import com.toipr.mapper.node.DataNodeMapper;
import com.toipr.model.node.DataNode;
import com.toipr.service.DefaultService;
import com.toipr.service.node.NodeService;
import com.toipr.util.HashHelper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultNodeService extends DefaultService implements NodeService {
    public DefaultNodeService(ApplicationContext context){
        super(context);
    }

    /**
     * 添加数据节点
     * @param node 节点对象
     * @return true=成功 false=失败
     */
    public boolean addNode(DataNode node){
        String idstr = getHid(node.getProtocol(), node.getRid(), node.getDataType(), node.getHost());
        if(idstr==null){
            return false;
        }
        node.setHid(idstr);

        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            int ret = mapper.addNode(node);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置主机状态
     * @param hid 主机ID
     * @param state 工作状态
     * @return true=成功 false=失败
     */
    public boolean setState(String hid, int state){
        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            int ret = mapper.removeNode(hid);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 增加访问次数，设置最后访问时间
     * @param hid 节点ID
     * @param visit 新增访问次数
     * @param lastAccess 最近访问时间
     * @return true=成功 false=失败
     */
    public boolean incVisitAndLastAccess(String hid, int visit, Date lastAccess){
        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            int ret = mapper.incVisitAndLastAccess(hid, visit, lastAccess);
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 测试节点是否能正常链接
     * @param node 节点对象
     * @return true='success' false=错误信息
     */
    public String testNode(DataNode node){
        try{
            try(SqlSession session = mybatis.getSession()) {
                DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);

                Map<String, Object> params = new HashMap<String, Object>();
                int count = mapper.count(params);
                return "success";
            }
        } catch(Exception ex){
            return ex.getMessage();
        }
    }

    /**
     * 测试节点是否工作正常
     * @param hid 节点ID
     * @return true='success' false=错误信息
     */
    public String testNode(String hid){
        try {
            DataNode node = null;
            try (SqlSession session = mybatis.getSession()) {
                DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
                node = mapper.getNode(hid);
                if (node == null) {
                    return "host is not existed";
                }
            }
            return testNode(node);
        } catch(Exception ex){
            return ex.getMessage();
        }
    }

    /**
     * 判断节点是否存在
     * @param protocol 传输协议
     * @param dataType 数据类型
     * @param host 主机地址与端口
     * @return 成功=HID 失败=null
     */
    public String nodeExists(String protocol, String dataType, String host){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("protocol", protocol);
        params.put("dataType", dataType);
        params.put("host", host);

        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            return mapper.nodeExists(params);
        }
    }

    /**
     * 删除主机节点
     * @param hid 主机ID
     * @return true=成功 false=失败
     */
    public boolean removeNode(String hid){
        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            int ret = mapper.removeNode(hid);
            if(ret>0) {
                session.commit();
            }
            return (ret>0);
        }
    }

    /**
     * 获取数据节点对象
     * @param hid 节点ID
     * @return 节点对象实例
     */
    public DataNode getNode(String hid){
        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            return mapper.getNode(hid);
        }
    }

    /**
     * 根据资源类型、数据类型与节点状态参数获取数据节点列表
     * @param params 查询条件参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param nodeList 节点列表
     * @return 符合条件的节点总数
     */
    public int getNodeList(Map<String, Object> params, int start, int count, List<DataNode> nodeList){
        try(SqlSession session = mybatis.getSession()){
            DataNodeMapper mapper = session.getMapper(DataNodeMapper.class);
            int total = mapper.count(params);
            if(total>0){
                List<DataNode> plist = mapper.getNodeList(params, start, count);
                if(plist!=null){
                    nodeList.addAll(plist);
                }
            }
            return total;
        }
    }

    protected String getHid(String protocol, String rid, String dataType, String host){
        String idstr = String.format("%s_%s_%s_%s", protocol, rid, dataType, host);
        try {
            byte[] hashBytes = HashHelper.computeHashBytes(idstr.getBytes("utf-8"), "SHA-256");
            idstr = HashHelper.getShortHashStr(hashBytes, 8);
            return idstr;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
