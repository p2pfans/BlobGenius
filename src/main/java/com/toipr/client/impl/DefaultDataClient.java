package com.toipr.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.toipr.client.*;
import com.toipr.model.data.DataConst;
import com.toipr.util.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

public class DefaultDataClient implements DataClient {
    /**
     * 退出标志
     */
    protected boolean isExit = false;

    /**
     * 工作线程
     */
    protected Thread worker = null;

    /**
     * 用户令牌
     */
    protected String token;
    /**
     * 过期时间
     */
    protected long expire;

    /**
     * 服务路由器
     */
    protected ServerFinder finder;

    /**
     * 进度事件监听器
     */
    protected ProgressListener listener;

    public DefaultDataClient(){
        this.finder = DataClients.getFinder();
    }

    /**
     * 关闭上传客户端
     */
    public void close(){
        isExit = true;
    }

    /**
     * 取消任务
     * @param task
     */
    public void cancel(Object task){
    }

    /**
     * 设置事件监听器
     * @param listener
     */
    public void addListener(ProgressListener listener){
        if(this.listener==null){
            this.listener = listener;
        } else if(this.listener!=listener){
            this.listener.setListener(listener);
        }
    }

    /**
     * 用户验证
     * @param uname 用户账号
     * @param upass 用户密码
     * @param args 其它参数
     * @return true=成功 false=失败
     */
    public boolean auth(String uname, String upass, Object... args){
        JSONObject json = new JSONObject();
        json.put("uname", uname);
        json.put("upass", upass);

        String link = finder.getServer("", "objects", false);
        link = link + "/object/auth";
        JSONObject ret = post(link, json);
        if(!isDone(ret)){
            return false;
        }

        this.token = ret.getString("token");
        this.expire = ret.getLong("expire");
        return true;
    }

    /**
     * 获取对象属性
     * @param target 对象实例
     * @return 属性映射表
     */
    public Map<String, Object> getObjectMap(Object target){
        if(target instanceof JSONObject){
            JSONObject json = (JSONObject)target;
            return json.getInnerMap();
        }
        return null;
    }

    public boolean isDone(JSONObject ret){
        if(ret==null || !ret.containsKey("status")){
            return false;
        }
        int code = ret.getInteger("status");
        if (code == 200) {
            return true;
        }
        return false;
    }

    public byte[] getData(String sUrl){
        HttpClient client = HttpClients.createDefault();

        HttpGet get = new HttpGet(sUrl);
        if(!Utils.isNullOrEmpty(token)) {
            get.addHeader("Cookie", String.format("token=%s", token));
        }

        try{
            HttpResponse resp = client.execute(get);
            HttpEntity ret = resp.getEntity();
            int size = (int)ret.getContentLength();
            try(InputStream ins = ret.getContent()){
                if(size>0){
                    byte[] data = new byte[size];
                    if(!Utils.readAll(data, data.length, ins)){
                        return null;
                    }
                    return data;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] data = new byte[8192];
                do{
                    int len = ins.read(data);
                    if(len==-1){
                        break;
                    }
                    baos.write(data, 0, len);
                }while(true);
                return baos.toByteArray();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public JSONObject getJson(String sUrl){
        HttpClient client = HttpClients.createDefault();

        HttpGet get = new HttpGet(sUrl);
        if(!Utils.isNullOrEmpty(token)) {
            get.addHeader("Cookie", String.format("token=%s;", token));
        }

        int times = 0;
        JSONObject json = null;
        do {
            try {
                HttpResponse resp = client.execute(get);
                HttpEntity ret = resp.getEntity();
                String text = EntityUtils.toString(ret);
                json = JSONObject.parseObject(text);
                int code = json.getInteger("status");
                if (code == 200) {
                    return json;
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            times++;
        }while(times<DataConst.def_fail_retry);
        return json;
    }

    public JSONObject post(String sUrl, JSONObject params){
        HttpClient client = HttpClients.createDefault();

        HttpPost post = new HttpPost(sUrl);
        if(!Utils.isNullOrEmpty(token)) {
            post.addHeader("Cookie", String.format("token=%s;", token));
        }

        StringEntity entity = new StringEntity(params.toString(), "utf-8");
        entity.setContentType("application/json");
        entity.setContentEncoding("utf-8");
        post.setEntity(entity);

        int times = 0;
        JSONObject json = null;
        do {
            try {
                HttpResponse resp = client.execute(post);
                HttpEntity ret = resp.getEntity();
                String text = EntityUtils.toString(ret);
                json = JSONObject.parseObject(text);
                int code = json.getInteger("status");
                if (code == 200) {
                    return json;
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            times++;
        }while(times<DataConst.def_fail_retry);
        return json;
    }
}
