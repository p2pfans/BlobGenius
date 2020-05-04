package com.toipr.service.search.impl;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataConst;
import com.toipr.model.data.DataObject;
import com.toipr.model.data.DataResource;
import com.toipr.model.user.OrgInfo;
import com.toipr.model.user.UserInfo;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.cache.CacheSubscriber;
import com.toipr.service.cache.MessageHandler;
import com.toipr.service.data.DataStoreService;
import com.toipr.service.data.DataStores;
import com.toipr.service.org.OrgService;
import com.toipr.service.org.OrgServices;
import com.toipr.service.resource.ResourceService;
import com.toipr.service.resource.ResourceServices;
import com.toipr.service.search.ObjectIndexer;
import com.toipr.service.user.UserService;
import com.toipr.service.user.UserServices;
import com.toipr.util.Utils;
import com.toipr.util.json.DataObjectHelper;
import com.toipr.util.threads.ThreadPoolWorker;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataObjectIndexer extends DefaultObjectIndexer implements MessageHandler {
    protected String channel;
    protected String collection;

    public DataObjectIndexer(String collection){
        this.collection = collection;
        this.channel = DataConst.DataType_Object;
    }

    @Override
    public boolean init(String[] hosts, Object... args){
        CacheSubscriber service = CacheServices.getSubscriber("events");
        if(service==null){
            return false;
        }
        /**
         * 订阅DataObject对象更新消息
         */
        service.subscribe(channel, true, this);
        return super.init(hosts, args);
    }

    /**
     * 索引一个对象
     * @param obj 对象实例
     * @return 成功=true 失败=false
     */
    public boolean index(Object obj){
        DataObject temp = (DataObject)obj;
        JSONObject json = DataObjectHelper.fromObject(temp);
        getInfoAdditional(json);

        IndexRequest req = new IndexRequest(collection).source(json.getInnerMap())
                .id(json.getString("uuid"));
        synchronized (lockObj) {
            lstRequest.add(req);
        }
        return true;
    }

    /**
     * 索引对象列表
     * @param oList 对象列表
     * @return 成功索引记录数量
     */
    public int index(List oList){
        synchronized (lockObj) {
            for (int i = 0; i < oList.size(); i++) {
                DataObject temp = (DataObject) oList.get(i);
                JSONObject json = DataObjectHelper.fromObject(temp);
                getInfoAdditional(json);

                IndexRequest req = new IndexRequest(collection).source(json.getInnerMap())
                        .id(json.getString("uuid"));
                lstRequest.add(req);
            }
        }
        return oList.size();
    }

    /**
     * 删除索引对象
     * @param uuid 对象ID
     */
    public void remove(String uuid){
        DeleteRequest req = new DeleteRequest(collection).id(uuid);
        synchronized (lockObj){
            lstDelete.add(req);
        }
    }

    /**
     * 消息处理回调
     * @param tid 任务ID
     * @param channel 频道名称
     * @param message 消息体
     */
    public void onMessage(String tid, String channel, String message){
        JSONObject json = (JSONObject)JSONObject.parse(message);

        String action = json.getString("action");
        if(action.compareTo("add")==0 || action.compareTo("update")==0){
            String jsonText = json.getString("object");
            if(Utils.isNullOrEmpty(jsonText)){
                return;
            }
            json = JSONObject.parseObject(jsonText);
            getInfoAdditional(json);
            IndexRequest req = new IndexRequest(collection).source(json.getInnerMap())
                    .id(json.getString("uuid"));
            synchronized (lockObj){
                lstRequest.add(req);
            }
        } else {
            String uuid = json.getString("uuid");
            if(Utils.isNullOrEmpty(uuid)){
                return;
            }
            remove(uuid);
        }
    }

    protected void getInfoAdditional(JSONObject json){
        String oid = json.getString("oid");
        if(!Utils.isNullOrEmpty(oid)){
            OrgInfo org = getOrg(oid);
            if (org != null) {
                json.put("org", org.getName());
            }
        }

        oid = json.getString("uid");
        if(!Utils.isNullOrEmpty(oid)){
            UserInfo user = getUser(oid);
            if(user != null){
                json.put("username", user.getNickname());
            }
        }

        oid = json.getString("rid");
        if(!Utils.isNullOrEmpty(oid)) {
            DataResource robj = getResource(oid);
            if(robj!=null){
                json.put("resource", robj.getName());
            }
        }
    }

    protected DataResource getResource(String rid){
        CacheServer server = CacheServices.getServer("resources", rid, false, null);
        if(server==null){
            return null;
        }

        DataResource item = null;
        if(server.exists(rid)) {
            item = (DataResource)server.getObject(rid, DataResource.class);
        }

        if(item==null){
            ResourceService service = ResourceServices.getInstance();
            if(service==null){
                return null;
            }
            item = service.getResource(rid);
            if(item!=null){
                server.addObject(rid, 1200*1000, CacheServer.ifNotExists, item);
            }
        }
        return item;
    }

    protected UserInfo getUser(String uid){
        CacheServer server = CacheServices.getServer("users", uid, false, null);
        if(server==null){
            return null;
        }

        UserInfo item = null;
        if(server.exists(uid)) {
            item = (UserInfo)server.getObject(uid, UserInfo.class);
        }

        if(item==null){
            UserService service = UserServices.getInstance();
            if(service==null){
                return null;
            }
            item = service.getUser(uid);
            if(item!=null){
                server.addObject(uid, 1200*1000, CacheServer.ifNotExists, item);
            }
        }
        return item;
    }

    protected OrgInfo getOrg(String oid){
        CacheServer server = CacheServices.getServer("orgs", oid, false, null);
        if(server==null){
            return null;
        }

        OrgInfo item = null;
        if(server.exists(oid)) {
            item = (OrgInfo)server.getObject(oid, OrgInfo.class);
        }

        if(item==null){
            OrgService service = OrgServices.getInstance();
            if(service==null){
                return null;
            }
            item = service.getOrg(oid);
            if(item!=null){
                server.addObject(oid, 1200*1000, CacheServer.ifNotExists, item);
            }
        }
        return item;
    }
}
