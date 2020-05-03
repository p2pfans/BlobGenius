package com.toipr.util.json;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataObject;

import java.util.Date;

public class DataObjectHelper {
    public static DataObject fromJson(boolean isNew, String jsonText){
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);
        return fromJson(isNew, json);
    }

    public static DataObject fromJson(boolean isNew, JSONObject json) {
        DataObject temp = new DataObject();
        if(!isNew) {
            temp.setDoid(json.getString("uuid"));
            temp.setDoid(json.getString("doid"));
        }
        temp.setState(json.getInteger("state"));
        temp.setFlags(json.getInteger("flags"));

        temp.setOid(json.getString("oid"));
        temp.setPid(json.getString("pid"));
        temp.setRid(json.getString("rid"));
        temp.setUid(json.getString("uid"));

        temp.setName(json.getString("name"));
        temp.setPath(json.getString("path"));
        temp.setHash(json.getString("hash"));
        temp.setMimeType(json.getString("mimeType"));

        temp.setSize(json.getInteger("size"));
        temp.setBlobSize(json.getInteger("blobSize"));
        temp.setDownload(json.getInteger("download"));

        temp.setTag(json.getString("tag"));
        temp.setVersion(json.getInteger("version"));

        temp.setIpAddr(json.getString("ipAddr"));
        if(!isNew) {
            temp.setTimeCreate(json.getDate("timeCreate"));
            temp.setLastAccess(json.getDate("lastAccess"));
            temp.setLastModify(json.getDate("lastModify"));
        } else {
            temp.setTimeCreate(new Date());
            temp.setLastAccess(new Date());
            temp.setLastModify(new Date());
        }
        return temp;
    }

    public static JSONObject fromObject(DataObject obj){
        JSONObject json = new JSONObject();
        json.put("uuid", obj.getUuid());
        json.put("doid", obj.getDoid());
        json.put("state", obj.getState());
        json.put("flags", obj.getFlags());

        json.put("directory", obj.isDirectory());
        json.put("compressed", obj.isCompressed());
        json.put("ciphered", obj.isCiphered());
        json.put("encoded", obj.isEncoded());

        json.put("oid", obj.getOid());
        json.put("pid", obj.getPid());
        json.put("rid", obj.getRid());
        json.put("uid", obj.getUid());

        json.put("name", obj.getName());
        json.put("path", obj.getPath());
        json.put("hash", obj.getHash());
        json.put("mimeType", obj.getMimeType());

        json.put("size", obj.getSize());
        json.put("blobSize", obj.getBlobSize());
        json.put("download", obj.getDownload());

        json.put("tag", obj.getTag());
        json.put("version", obj.getVersion());

        json.put("ipAddr", obj.getIpAddr());
        json.put("timeCreate", obj.getTimeCreate());
        json.put("lastAccess", obj.getLastAccess());
        json.put("lastModify", obj.getLastModify());
        return json;
    }
}
