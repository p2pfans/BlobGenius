package com.toipr.util.json;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataResource;

import java.util.Date;

public class DataResourceHelper {
    public static JSONObject fromObject(DataResource obj){
        JSONObject temp = new JSONObject();
        temp.put("rid", obj.getRid());
        temp.put("state", obj.getState());
        temp.put("code", obj.getCode());
        temp.put("name", obj.getName());
        temp.put("oid", obj.getOid());

        temp.put("tags", obj.getTags());
        temp.put("brief", obj.getBrief());

        temp.put("maxCount", obj.getMaxCount());
        temp.put("maxSpace", obj.getMaxSpace());

        temp.put("totalVisit", obj.getTotalVisit());
        temp.put("totalCount", obj.getTotalCount());
        temp.put("totalSpace", obj.getTotalSpace());

        temp.put("ipAddr", obj.getIpAddr());
        temp.put("uidCreate", obj.getUidCreate());
        temp.put("timeCreate", obj.getTimeCreate());
        temp.put("lastAccess", obj.getLastAccess());
        return temp;
    }

    public static DataResource fromJson(boolean isNew, String jsonText){
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        return fromJson(isNew, pobj);
    }

    public static DataResource fromJson(boolean isNew, JSONObject obj){
        DataResource res = new DataResource();
        if(!isNew) {
            res.setRid(obj.getString("rid"));
            res.setOid(obj.getString("oid"));
            res.setState(obj.getInteger("state"));
        }

        String str = obj.getString("code");
        if(str==null || str.length()==0){
            return null;
        }
        res.setCode(str);

        str = obj.getString("name");
        if(str==null || str.length()==0){
            return null;
        }
        res.setName(str);

        res.setTags(obj.getString("tags"));
        res.setBrief(obj.getString("brief"));

        res.setMaxCount(obj.getLong("maxCount"));
        res.setMaxSpace(obj.getLong("maxSpace"));
        res.setTotalVisit(obj.getLong("totalVisit"));
        res.setTotalCount(obj.getLong("totalCount"));
        res.setTotalSpace(obj.getLong("totalSpace"));

        if(isNew){
            res.setTimeCreate(new Date());
            res.setLastAccess(new Date());
        }
        return res;
    }
}
