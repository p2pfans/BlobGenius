package com.toipr.service.user.impl;

import com.toipr.mapper.user.UserInfoMapper;
import com.toipr.model.user.UserInfo;
import com.toipr.service.DefaultService;
import com.toipr.service.user.UserService;
import com.toipr.util.HashHelper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DefaultUserService extends DefaultService implements UserService {
    public DefaultUserService(ApplicationContext context){
        super(context);
    }

    /**
     * 判断用户是否存在
     * @param uname 用户名称
     * @return true=存在 false=不存在
     */
    public String userExists(String uname) {
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            return mapper.userExists(uname, "user_info");
        }
    }

    /**
     * 注册新用户
     * @param user 用户对象
     * @return true=成功 false=失败
     */
    public boolean registUser(UserInfo user){
        String uid = getUid(user.getUsername());
        if(uid==null){
            return false;
        }
        user.setUid(uid);

        try {
            String pass = HashHelper.computeHash(user.getPassword().getBytes("utf-8"), "SHA-256");
            pass = HashHelper.getShortHashStr(pass, 8);
            user.setPassword(pass);
        }catch(Exception ex){
            return false;
        }

        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.addUser(user, "user_info");
            if(ret>0) {
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置用户状态
     * @param uid 用户ID
     * @param state 用户状态
     * @return true=成功 false=失败
     */
    public boolean setState(String uid, int state){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.setState(uid, state, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置用户等级
     * @param uid 用户ID
     * @param level 用户等级
     * @return true=成功 false=失败
     */
    public boolean setLevel(String uid, int level){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.setLevel(uid, level, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置用户机构与职位
     * @param uid 用户代码
     * @param org 企业名称
     * @param oid 机构ID
     * @param title 担任职务
     * @return true=成功 false=失败
     */
    public boolean setOrgAndTitle(String uid, String org, String oid, String title){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.setOrgAndTitle(uid, org, oid, title, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置用户Email
     * @param uid 用户ID
     * @param email 电子邮箱
     * @return true=成功 false=失败
     */
    public boolean setEmail(String uid, String email){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.setEmail(uid, email, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 设置用户手机号码
     * @param uid 用户ID
     * @param phone 手机号码
     * @return true=成功 false=失败
     */
    public boolean setPhone(String uid, String phone){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.setPhone(uid, phone, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 删除用户
     * @param uid 用户ID
     * @return true=成功 false=失败
     */
    public boolean removeUser(String uid){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.removeUser(uid, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 增加登录次数，设置登录IP地址与时间
     * @param uid 用户ID
     * @param ipAddr IP地址
     * @param lastAccess 访问时间
     * @return true=成功 false=失败
     */
    public boolean incLoginAndLastAccess(String uid, String ipAddr, Date lastAccess){
        try(SqlSession session = mybatis.getSession()){
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            int ret = mapper.incLoginAndLastAccess(uid, ipAddr, lastAccess, "user_info");
            if(ret>0){
                session.commit();
                return true;
            }
        }
        return false;
    }

    /**
     * 根据电子邮箱获取用户信息
     * @param uid 用户ID
     * @return 成功=用户对象 失败=null
     */
    public UserInfo getUser(String uid){
        try(SqlSession session = mybatis.getSession()) {
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            return mapper.getUser(uid, "user_info");
        }
    }

    /**
     * 根据电子邮箱获取用户信息
     * @param email 电话号码-手机
     * @return 成功=用户对象 失败=null
     */
    public UserInfo getUserByEmail(String email){
        try(SqlSession session = mybatis.getSession()) {
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            return mapper.getUserByEmail(email, "user_info");
        }
    }

    /**
     * 根据电话号码获取用户信息
     * @param phone 电话号码-手机
     * @return 成功=用户对象 失败=null
     */
    public UserInfo getUserByPhone(String phone){
        try(SqlSession session = mybatis.getSession()) {
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            return mapper.getUserByPhone(phone, "user_info");
        }
    }

    /**
     * 认证用户名密码,并获取用户信息
     * @param uname 用户账号
     * @param upass 用户密码
     * @return 成功=用户对象 失败=null
     */
    public UserInfo checkUser(String uname, String upass){
        try{
            upass = HashHelper.computeHash(upass.getBytes("utf-8"), "SHA-256");
            upass = HashHelper.getShortHashStr(upass, 8);
        }catch(Exception ex){
            ;
        }

        try(SqlSession session = mybatis.getSession()) {
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            return mapper.checkUser(uname, upass, "user_info");
        }
    }

    /**
     * 根据参数查询用户列表
     * @param params 参数列表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 用户列表
     * @return 用户数量
     */
    public int getUserList(Map<String, Object> params, int start, int count, List<UserInfo> rlist){
        int total = 0;
        try(SqlSession session = mybatis.getSession()) {
            UserInfoMapper mapper = session.getMapper(UserInfoMapper.class);
            total = mapper.count(params, "user_info");
            if(total>0){
                List<UserInfo> plist = new ArrayList<UserInfo>();
                plist = mapper.getUserList(params, start, count, "user_info");
                if(plist!=null){
                    rlist.addAll(plist);
                }
            }
        }
        return total;
    }

    protected String getUid(String username){
        String idstr;
        try{
            byte[] hashBytes = HashHelper.computeHashBytes(username.getBytes("utf-8"), "SHA-256");
            idstr = HashHelper.getShortHashStr(hashBytes, 8);
            return idstr;
        } catch(Exception ex){
            ;
        }
        return null;
    }
}
