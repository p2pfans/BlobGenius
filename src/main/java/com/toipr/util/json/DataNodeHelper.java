package com.toipr.util.json;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.node.DataNode;
import com.toipr.util.Utils;

import java.util.Date;

public class DataNodeHelper {
    public static DataNode fromJson(boolean isNew, String jsonText){
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);
        return fromJson(isNew, json);
    }

    public static DataNode fromJson(boolean isNew, JSONObject obj){
        String str = "";
        DataNode node = new DataNode();
        if(!isNew) {
            node.setHid(obj.getString("hid"));
            node.setState(obj.getInteger("state"));
        }

        str = obj.getString("host");
        if(str==null || str.length()<10){
            return null;
        }
        node.setHost(str);
        node.setHostid(obj.getString("hostid"));

        String dataType = obj.getString("dataType");
        if(dataType==null || dataType.length()==0){
            return null;
        }
        node.setDataType(dataType);

        str = obj.getString("protocol");
        if(str==null || str.length()==0){
            str = "jdbc";
        }
        node.setProtocol(str);

        str = obj.getString("dbType");
        if(str==null || str.length()==0){
            str = "mysql";
        }
        node.setDbType(str);

        str = obj.getString("rid");
        if(str==null || str.length()==0){
            str = "default";
        }
        node.setRid(str);

        str = obj.getString("filePath");
        if(!Utils.isNullOrEmpty(str)){
            node.setFilePath(str);
        }

        node.setHid(obj.getString("dbUser"));
        node.setHid(obj.getString("dbPass"));
        node.setState(obj.getInteger("master"));
        node.setUseUnicode(obj.getInteger("useUnicode"));
        node.setDistKey(obj.getString("distKey"));
        node.setTimeZone(obj.getString("timeZone"));
        node.setDriverClass(obj.getString("driverClassName"));
        if(isNew){
            node.setTmCreate(new Date());
            node.setLastAlive(new Date());
        }
        return node;
    }

    public static JSONObject fromObject(DataNode node){
        JSONObject temp = new JSONObject();
        temp.put("hid", node.getHid());
        temp.put("state", node.getState());
        temp.put("host", node.getHost());
        temp.put("hostid", node.getHostid());
        temp.put("protocol", node.getProtocol());

        temp.put("dataType", node.getDataType());
        temp.put("dbType", node.getDbType());
        temp.put("rid", node.getRid());
        temp.put("dbUser", node.getDbUser());
        temp.put("dbPass", node.getDbPass());
        temp.put("path", node.getFilePath());

        temp.put("master", node.getMaster());
        temp.put("distKey", node.getDistKey());
        temp.put("useUnicode", node.getUseUnicode());
        temp.put("timeZone", node.getTimeZone());
        temp.put("driverClassName", node.getDriverClass());
        temp.put("totalVisit", node.getTotalVisit());
        temp.put("timeCreate", node.getTmCreate());
        temp.put("lastAlive", node.getLastAlive());
        return temp;
    }
}
