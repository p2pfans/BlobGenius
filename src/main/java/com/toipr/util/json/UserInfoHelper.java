package com.toipr.util.json;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.user.UserInfo;

import java.util.Date;

public class UserInfoHelper {
    public static JSONObject fromObject(UserInfo obj){
        JSONObject temp = new JSONObject();
        temp.put("uid", obj.getUid());
        temp.put("state", obj.getState());
        temp.put("level", obj.getLevel());
        temp.put("sex", obj.getSex());
        temp.put("username", obj.getUsername());
        temp.put("nickname", obj.getNickname());

        temp.put("oid", obj.getOid());
        temp.put("org", obj.getOrg());
        temp.put("title", obj.getTitle());
        temp.put("email", obj.getEmail());
        temp.put("phone", obj.getPhone());

        temp.put("maxCount", obj.getMaxCount());
        temp.put("maxSpace", obj.getMaxSpace());

        temp.put("totalLogin", obj.getTotalLogin());
        temp.put("timeCreate", obj.getTimeCreate());
        temp.put("lastAccess", obj.getLastAccess());
        temp.put("lastIpAddr", obj.getLastIpAddr());
        return temp;
    }

    public static UserInfo fromJson(boolean isNew, String jsonText) {
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);
        return fromJson(isNew, json);
    }

    public static UserInfo fromJson(boolean isNew, JSONObject obj){
        UserInfo uobj = new UserInfo();
        String name = obj.getString("username");
        if(name.length()<6){
            return null;
        }

        if(!isNew){
            uobj.setUid(obj.getString("uid"));
            uobj.setState(obj.getInteger("state"));
        }
        uobj.setLevel(obj.getInteger("level"));

        String pass = obj.getString("password");
        if(pass==null || pass.length()<6){
            return null;
        }
        uobj.setUsername(name);
        uobj.setPassword(pass);
        uobj.setNickname(obj.getString("nickname"));
        uobj.setSex(Integer.parseInt(obj.getString("sex")));

        uobj.setOid(obj.getString("oid"));
        uobj.setOrg(obj.getString("org"));
        uobj.setTitle(obj.getString("title"));
        uobj.setEmail(obj.getString("email"));
        uobj.setPhone(obj.getString("phone"));

        if(isNew) {
            uobj.setTimeCreate(new Date());
            uobj.setLastAccess(uobj.getTimeCreate());
        }
        return uobj;
    }
}
