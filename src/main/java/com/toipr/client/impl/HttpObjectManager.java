package com.toipr.client.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.client.ObjectManager;

import com.toipr.model.data.DataConst;
import com.toipr.util.HashHelper;
import com.toipr.util.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpObjectManager extends DefaultDataClient implements ObjectManager {
    public HttpObjectManager(){

    }

    /**
     * 创建目录对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param flags 目录属性
     * @param name 对象名称
     * @param path  目录路径
     * @return 目录对象
     */
    public Object createDir(String rid, String pid, int flags, String name, String path){
        JSONObject json = new JSONObject();
        json.put("rid", rid);
        json.put("pid", pid);
        json.put("flags", flags);
        json.put("name", name);
        json.put("path", path);

        String link = finder.getServer(rid, DataConst.DataType_Object, false);
        link = link + "/object/createDir";
        JSONObject ret = post(link, json);
        if(!isDone(ret)){
            return null;
        }
        return ret.getJSONObject("item");
    }

    /**
     * 根据文件生成数字对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param flags 对象标志
     * @param name 对象名称
     * @param path  目录路径
     * @param file 文件对象
     * @return JSONObject
     */
    public Object createObject(String rid, String pid, int flags, String name, String path, File file){
        try{
            Path path2 = Paths.get(file.getAbsolutePath());

            int blobSize = DataConst.DataBlob_DefSize;

            String hash = "";
            String mime = Files.probeContentType(path2);
            if(file.isDirectory()){
                flags |= DataConst.DataFlags_Directory;
            } else if(file.length()>0){
                hash = HashHelper.computeHash(file.getAbsolutePath(), "SHA-256");
            }
            return createObject(rid, pid, flags, name, path, hash, file.length(), blobSize, mime);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 根据参数生成数字对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param flags 对象标志
     * @param name 对象名称
     * @param path  目录路径
     * @param hash 数据校验码
     * @param size 对象大小
     * @param blobSize 数据块大小
     * @param mime 数据类型
     * @return JSONObject
     */
    public synchronized Object createObject(String rid, String pid, int flags, String name, String path, String hash, long size, int blobSize, String mime){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("rid", rid);
        params.put("pid", pid);
        params.put("flags", flags);
        params.put("name", name);
        params.put("path", path);
        params.put("size", (int)size);
        if(blobSize>0){
            params.put("blobSize", blobSize);
        } else {
            params.put("blobSize", DataConst.DataBlob_DefSize);
        }
        if(!Utils.isNullOrEmpty(hash)){
            params.put("hash", hash);
        }

        if(!Utils.isNullOrEmpty(mime)){
            params.put("mimeType", mime);
        } else {
            int pos = name.lastIndexOf(".");
            if(pos>0){
                mime = name.substring(pos+1);
                params.put("mimeType", mime);
            }
        }
        return createObject(rid, pid, params);
    }

    /**
     * 根据参数生成数字对象
     * @param rid 资源ID
     * @param pid 父目录ID
     * @param params 对象属性
     * @return JSONObject
     */
    public Object createObject(String rid, String pid, Map<String, Object> params){
        JSONObject json = new JSONObject(params);
        if(!json.containsKey("rid")){
            json.put("rid", rid);
        }
        if(!json.containsKey("pid")){
            json.put("pid", pid);
        }

        String link = finder.getServer(rid, DataConst.DataType_Object, false);
        link = link + "/object/create";
        JSONObject ret = post(link, json);
        if(!isDone(ret)){
            return null;
        }
        return ret.getJSONObject("item");
    }

    /**
     * 获取数字对象的块索引队列
     * @param rid 资源ID
     * @param uuid 数字对象ID
     * @return 块索引队列JSONObject
     */
    public List<Object> getBlobIds(String rid, String uuid){
        String link = finder.getServer(rid, "blobids", false);
        link += String.format("/data/download/blobids/%s/%s", rid, uuid);

        JSONObject ret = getJson(link);
        if(!isDone(ret)){
            return null;
        }

        int total = ret.getInteger("total");
        if(total<=0){
            return null;
        }

        List<Object> rlist = new ArrayList<Object>();
        JSONArray myarr = ret.getJSONArray("docs");
        for(int i=0; i<myarr.size(); i++){
            rlist.add(myarr.get(i));
        }
        return rlist;
    }

    /**
     * 删除对象
     * @param rid 资源ID
     * @param uuid 对象ID
     * @return true=成功 false=失败
     */
    public boolean removeObject(String rid, String uuid){
        JSONObject json = new JSONObject();
        json.put("rid", rid);
        json.put("uuid", uuid);

        String link = finder.getServer(rid, DataConst.DataType_Object, false);
        link = link + "/object/delete";
        JSONObject ret = post(link, json);
        return isDone(ret);
    }

    /**
     * 设置对象状态
     * @param rid 资源ID
     * @param uuid 对象ID
     * @param state 状态码
     * @return true=成功 false=失败
     */
    public boolean setState(String rid, String uuid, int state){
        JSONObject json = new JSONObject();
        json.put("rid", rid);
        json.put("uuid", uuid);
        json.put("state", state);

        String link = finder.getServer(rid, DataConst.DataType_Object, false);
        link = link + "/object/state";
        JSONObject ret = post(link, json);
        return isDone(ret);
    }

    /**
     * 从服务器获取对象
     * @param rid 资源ID
     * @param uuid 对象ID
     * @return 对象实例
     */
    public Object getObject(String rid, String uuid){
        JSONObject json = new JSONObject();
        json.put("rid", rid);
        json.put("uuid", uuid);

        String link = finder.getServer(rid, DataConst.DataType_Object, false);
        link = link + "/object/get";
        JSONObject ret = post(link, json);
        if(!isDone(ret)){
            return null;
        }
        return ret.getJSONObject("item");
    }

    /**
     * 根据参数获取对象列表
     * @param params 参数列表
     * @param start 开始记录
     * @param count 记录数量
     * @param rlist 记录列表
     * @return 记录总数
     */
    public int queryObjects(Map<String, Object> params, int start, int count, List<Object> rlist){
        JSONObject json = new JSONObject(params);
        json.put("start", start);
        json.put("count", count);

        String rid = "";
        if(params.containsKey("rid")){
            rid = (String)params.get("rid");
        }
        String link = finder.getServer(rid, DataConst.DataType_Object, false);
        link = link + "/object/list";
        JSONObject ret = post(link, json);
        if(!isDone(ret)){
            return 0;
        }

        int total = ret.getInteger("total");
        JSONArray myarr = ret.getJSONArray("docs");
        for(int i=0; i<myarr.size(); i++){
            rlist.add(myarr.get(i));
        }
        return total;
    }
}
