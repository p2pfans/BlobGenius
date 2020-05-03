package com.toipr.mapper.user;

import com.toipr.model.user.UserInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserInfoMapper {
    /**
     * 增加用户对象
     * @param item 用户对象
     * @param table 数据表名称
     * @return 成功返回1
     */
    int addUser(@Param("item")UserInfo item, @Param("table")String table);

    /**
     * 判断用户是否存在
     * @param name 用户名称
     * @param table 数据表名称
     * @return 成功=记录ID，失败=null
     */
    String userExists(@Param("name")String name, @Param("table")String table);

    /**
     * 删除uid用户对象
     * @param uid 用户ID
     * @param table 数据表名称
     * @return 成功返回1
     */
    int removeUser(@Param("uid")String uid, @Param("table")String table);

    /**
     * 设置用户等级
     * @param uid 用户ID
     * @param level 用户等级
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setLevel(@Param("uid")String uid, @Param("level")int level, @Param("table")String table);

    /**
     * 设置用户账号状态
     * @param uid 用户ID
     * @param state 账号状态
     * @param table 数据表名称
     * @return 成功返回1
     */
    int setState(@Param("uid")String uid, @Param("state")int state, @Param("table")String table);

    /**
     * 设置用户所在机构及担任职位
     * @param uid 用户ID
     * @param org 机构名称
     * @param oid 机构ID
     * @param title 担任职位
     * @param table 数据名称
     * @return 成功=1 失败=0
     */
    int setOrgAndTitle(@Param("uid")String uid, @Param("org")String org, @Param("oid")String oid, @Param("title")String title, @Param("table")String table);

    /**
     * 设置用户手机号码
     * @param uid 用户ID
     * @param phone 电话号码
     * @param table 数据表名称
     * @return 成功=1 失败=0
     */
    int setPhone(@Param("uid")String uid, @Param("phone")String phone, @Param("table")String table);

    /**
     * 设置用户电子邮箱
     * @param uid 用户ID
     * @param email 电子邮箱
     * @param table 数据表名称
     * @return 成功=1 失败=0
     */
    int setEmail(@Param("uid")String uid, @Param("email")String email, @Param("table")String table);

    /**
     * 增加登录次数，设置最后登录IP与地址
     * @param uid 用户ID
     * @param ipAddr IP地址
     * @param lastAccess 最后访问时间
     * @param table 数据表名称
     * @return 成功=1 失败=0
     */
    int incLoginAndLastAccess(@Param("uid")String uid, @Param("ipAddr")String ipAddr, @Param("lastAccess")Date lastAccess, @Param("table")String table);

    /**
     * 获取用户信息
     * @param uid 用户ID
     * @param table 数据表名称
     * @return 用户对象实例或null
     */
    UserInfo getUser(@Param("uid")String uid, @Param("table")String table);

    /**
     * 获取用户信息
     * @param name 用户账号
     * @param pass 用户密码
     * @param table 数据表名称
     * @return 用户对象实例或null
     */
    UserInfo checkUser(@Param("name")String name, @Param("pass")String pass,@Param("table")String table);

    /**
     * 获取用户信息
     * @param email 电子邮箱
     * @param table 数据表名称
     * @return 用户对象实例或null
     */
    UserInfo getUserByEmail(@Param("email")String email, @Param("table")String table);

    /**
     * 获取用户信息
     * @param phone 电话号码
     * @param table 数据表名称
     * @return 用户对象实例或null
     */
    UserInfo getUserByPhone(@Param("phone")String phone, @Param("table")String table);

    /**
     * 统计符合查询条件的记录数量
     * @param params 查询条件
     * @param table 数据表名称
     * @return 记录数量
     */
    int count(@Param("params") Map<String, Object> params, @Param("table")String table);

    /**
     * 获取用户列表
     * @param params 参数列表
     * @param start 开始记录号
     * @param count 获取记录数
     * @param table 数据表名称
     * @return 用户列表
     */
    List<UserInfo> getUserList(@Param("params") Map<String, Object> params, @Param("start")int start, @Param("count")int count, @Param("table")String table);
}
