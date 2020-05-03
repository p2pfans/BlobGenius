package com.toipr.util.json;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.user.OrgInfo;

import java.util.Date;

public class OrgInfoHelper {
    public static OrgInfo fromJson(boolean isNew, String jsonText){
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);
        return fromJson(isNew, json);
    }

    public static OrgInfo fromJson(boolean isNew, JSONObject obj){
        OrgInfo temp = new OrgInfo();
        if(!isNew){
            temp.setOid(obj.getString("oid"));
            temp.setState(obj.getInteger("state"));
        }
        temp.setLevel(obj.getInteger("level"));
        temp.setPid(obj.getString("pid"));

        String str = obj.getString("name");
        if(str==null || str.length()==0){
            return null;
        }
        temp.setName(str);
        temp.setAbbr(obj.getString("abbr"));

        temp.setContact(obj.getString("contact"));
        temp.setTitle(obj.getString("title"));
        temp.setEmail(obj.getString("email"));
        temp.setPhone(obj.getString("phone"));
        temp.setLogo(obj.getString("logo"));
        temp.setWebsite(obj.getString("website"));

        temp.setMaxCount(obj.getLong("maxCount"));
        temp.setMaxSpace(obj.getLong("maxSpace"));

        temp.setUidAdmin(obj.getString("uidAdmin"));
        if(isNew){
            temp.setTimeCreate(new Date());
        }
        return temp;
    }

    public static JSONObject fromObject(OrgInfo obj){
        JSONObject temp = new JSONObject();
        temp.put("oid", obj.getOid());
        temp.put("pid", obj.getPid());
        temp.put("state", obj.getState());
        temp.put("level", obj.getLevel());

        temp.put("name", obj.getName());
        temp.put("abbr", obj.getAbbr());

        temp.put("contact", obj.getContact());
        temp.put("title", obj.getTitle());
        temp.put("email", obj.getEmail());
        temp.put("phone", obj.getPhone());
        temp.put("logo", obj.getLogo());
        temp.put("website", obj.getWebsite());

        temp.put("maxCount", obj.getMaxCount());
        temp.put("maxSpace", obj.getMaxSpace());

        temp.put("uidAdmin", obj.getUidAdmin());
        temp.put("timeCreate", obj.getTimeCreate());
        return temp;
    }
}
