package com.toipr.service.token.impl;

import com.toipr.model.user.UserInfo;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.token.TokenService;
import com.toipr.service.user.UserService;
import com.toipr.service.user.UserServices;
import com.toipr.util.HashHelper;

import java.util.Date;
import java.util.Map;

public class DefaultTokenService implements TokenService {
    protected String dbname = "users";

    /**
     * 初始化令牌服务
     * @param dbname 缓存数据库名
     * @param args 可选参数
     * @return true=成功 false=失败
     */
    public boolean init(String dbname, Object... args){
        this.dbname = dbname;
        return true;
    }

    /**
     * 删除授权令牌
     * @param token 令牌
     * @return true=成功 false=失败
     */
    public boolean remove(String token){
        CacheServer server = CacheServices.getServer(dbname, token, true, null);
        if(server==null){
            return false;
        }
        server.removeCache(token);
        return true;
    }

    /**
     * @param token 令牌ID
     * @return true=存在 false=不存在
     */
    public boolean exists(String token){
        CacheServer server = CacheServices.getServer(dbname, token, false, null);
        if(server==null){
            return false;
        }
        return server.exists(token);
    }

    /**
     * 获取令牌信息
     * @param token 令牌ID
     * @param rights 令牌权限
     * @return true=成功 false=失败
     */
    public boolean getToken(String token, Map<String, Object> rights){
        CacheServer server = CacheServices.getServer(dbname, token, false, null);
        if(server==null){
            return false;
        }

        Map<String, String> maps = server.getMap(token);
        if(maps==null){
            return false;
        }

        for(Map.Entry<String, String> item : maps.entrySet()){
            String name = item.getKey();
            if(name.compareTo("expire")==0){
                long value = Long.parseLong(item.getValue());
                rights.put(name, value);
            } else {
                rights.put(name, item.getValue());
            }
        }
        return server.exists(token);
    }

    /**
     * 根据用户名密码获取授权
     * @param uname 用户名
     * @param upass 密码
     * @param ipAddr 访问IP
     * @param rights 授权信息
     * @return 成功=令牌ID 失败=null
     */
    public String authUser(String uname, String upass, String ipAddr, Map<String, Object> rights){
        UserService service = UserServices.getInstance();
        UserInfo user = service.checkUser(uname, upass);
        if(user==null){
            return "401";
        }
        service.incLoginAndLastAccess(user.getUid(), ipAddr, new Date());

        try {
            Date tmNow = new Date();
            Date tmExp = new Date(tmNow.getTime() + 3 * 60 * 60 * 1000);
            String token = String.format("%s_%d", user.getUid(), tmExp.getTime());
            token = "tok" + HashHelper.computeHash(token.getBytes(), "md5");

            CacheServer server = CacheServices.getServer("users", token, true, null);
            if(!server.exists(token)) {
                if(!server.addMapCache(token, 0, "expire", tmExp.getTime())){
                    return "500";
                }
                server.addMapCache(token, 0, "uid", user.getUid());
                server.addMapCache(token, 0, "oid", user.getOid());
            }

            //TODO 添加可以访问的资源列表与权限
            rights.put("expire", tmExp.getTime());
            rights.put("uid", user.getUid());
            rights.put("oid", user.getOid());
            return token;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return "500";
    }

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
    public boolean isActionAllowed(String action, String token, String rid, String doid, String ipAddr, Map<String, Object> rights){
        CacheServer server = CacheServices.getServer(dbname, token, false, doid);
        if(server==null){
            return false;
        }

        long tmExpire = server.getLong(token, "expire", 0);
        if(tmExpire==0){
            return false;
        }

        Date tmNow = new Date();
        if(tmNow.getTime()>tmExpire){
            return false;
        }

        //TODO 增加操作指令与资源访问控制逻辑, 增加IP访问控制
        Map<String, String> maps = server.getMap(token);
        if(maps==null || !maps.containsKey("uid") || !maps.containsKey("oid")){
            return false;
        }
        if(rights!=null){
            rights.put("uid", maps.get("uid"));
            rights.put("oid", maps.get("oid"));
        }
        return true;
    }
}
