package com.toipr.service.user;

import com.toipr.model.user.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * 判断用户是否存在
     * @param uname 用户名称
     * @return 存在=用户ID 不存在=null
     */
    String userExists(String uname);

    /**
     * 注册新用户
     * @param user 用户对象
     * @return true=成功 false=失败
     */
    boolean registUser(UserInfo user);

    /**
     * 设置用户状态
     * @param uid 用户ID
     * @param state 用户状态
     * @return true=成功 false=失败
     */
    boolean setState(String uid, int state);

    /**
     * 设置用户等级
     * @param uid 用户ID
     * @param level 用户等级
     * @return true=成功 false=失败
     */
    boolean setLevel(String uid, int level);

    /**
     * 设置用户机构与职位
     * @param uid 用户代码
     * @param org 企业名称
     * @param oid 机构ID
     * @param title 担任职务
     * @return true=成功 false=失败
     */
    boolean setOrgAndTitle(String uid, String org, String oid, String title);

    /**
     * 设置用户Email
     * @param uid 用户ID
     * @param email 电子邮箱
     * @return true=成功 false=失败
     */
    boolean setEmail(String uid, String email);

    /**
     * 设置用户手机号码
     * @param uid 用户ID
     * @param phone 手机号码
     * @return true=成功 false=失败
     */
    boolean setPhone(String uid, String phone);

    /**
     * 删除用户
     * @param uid 用户ID
     * @return true=成功 false=失败
     */
    boolean removeUser(String uid);

    /**
     * 根据电子邮箱获取用户信息
     * @param uid 用户ID
     * @return 成功=用户对象 失败=null
     */
    UserInfo getUser(String uid);

    /**
     * 根据电子邮箱获取用户信息
     * @param email 电话号码-手机
     * @return 成功=用户对象 失败=null
     */
    UserInfo getUserByEmail(String email);

    /**
     * 根据电话号码获取用户信息
     * @param phone 电话号码-手机
     * @return 成功=用户对象 失败=null
     */
    UserInfo getUserByPhone(String phone);

    /**
     * 认证用户名密码,并获取用户信息
     * @param uname 用户账号
     * @param upass 用户密码
     * @return 成功=用户对象 失败=null
     */
    UserInfo checkUser(String uname, String upass);

    /**
     * 增加登录次数，设置登录IP地址与时间
     * @param uid 用户ID
     * @param ipAddr IP地址
     * @param lastAccess 访问时间
     * @return true=成功 false=失败
     */
    boolean incLoginAndLastAccess(String uid, String ipAddr, Date lastAccess);

    /**
     * 根据参数查询用户列表
     * @param params 参数列表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 用户列表
     * @return 用户数量
     */
    int getUserList(Map<String, Object> params, int start, int count, List<UserInfo> rlist);
}
