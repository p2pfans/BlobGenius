package com.toipr.service.token;

import java.util.Map;

public interface TokenService {
    /**
     * 初始化令牌服务
     * @param dbname 缓存数据库名
     * @param args 可选参数
     * @return true=成功 false=失败
     */
    boolean init(String dbname, Object... args);

    /**
     * 删除授权令牌
     * @param token 令牌
     * @return true=成功 false=失败
     */
    boolean remove(String token);

    /**
     * @param token 令牌ID
     * @return true=存在 false=不存在
     */
    boolean exists(String token);

    /**
     * 获取令牌信息
     * @param token 令牌ID
     * @param rights 令牌权限
     * @return true=成功 false=失败
     */
    boolean getToken(String token, Map<String, Object> rights);

    /**
     * 根据用户名密码获取授权
     * @param uname 用户名
     * @param upass 密码
     * @param ipAddr 访问IP
     * @param rights 授权信息
     * @return 成功=令牌ID 失败=null
     */
    String authUser(String uname, String upass, String ipAddr, Map<String, Object> rights);

    /**
     * 是否拥有对象操作授权
     * @param action 操作名称
     * @param token 令牌ID
     * @param rid 资源ID
     * @param doid 数字资源ID
     * @param ipAddr 访问IP
     * @param rights 授权信息
     * @return true=允许授权 false=拒绝授权
     */
    boolean isActionAllowed(String action, String token, String rid, String doid, String ipAddr, Map<String, Object> rights);
}
