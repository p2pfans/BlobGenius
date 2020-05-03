package com.toipr.service.search.impl;

import com.toipr.service.cache.CacheServices;
import com.toipr.service.cache.CacheSubscriber;
import com.toipr.service.search.ObjectIndexer;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class DefaultObjectIndexer implements ObjectIndexer, Runnable {
    protected RestHighLevelClient esClient;

    protected Object lockObj = new Object();
    protected List<IndexRequest> lstRequest = new ArrayList<IndexRequest>();
    protected List<DeleteRequest> lstDelete = new ArrayList<DeleteRequest>();

    public DefaultObjectIndexer(){

    }

    /**
     * 初始化对象索引器
     * @param hosts 服务器地址
     * @param args 其它参数
     * @return 成功=true 失败=false
     */
    public boolean init(String[] hosts, Object... args){
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for(int i=0; i<hosts.length; i++){
            httpHosts[i] = HttpHost.create(hosts[i]);
        }

        RestClientBuilder builder = RestClient.builder(httpHosts);
        esClient = new RestHighLevelClient(builder);

        /**
         * 定时数据索引，每3秒清空一次队列
         */
        ThreadPoolWorker.scheduleWithFixedDelay(this, 5000, 3000, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 消息处理回调
     * @param tid 任务ID
     * @param pattern 频道模式
     * @param channel 频道名称
     * @param message 消息体
     */
    public void onMessage(String tid, String pattern, String channel, String message){
        onMessage(tid, channel, message);
    }

    @Override
    public void run(){
        if(lstRequest.size()==0 && lstDelete.size()==0) {
            return;
        }

        BulkRequest bulk = null;
        synchronized (lockObj){
            if(lstRequest.size()==0 && lstDelete.size()==0) {
                return;
            }
            bulk = new BulkRequest();
            if(lstRequest.size()>0) {
                for (IndexRequest req : lstRequest) {
                    bulk.add(req);
                }
                lstRequest.clear();
            }
            if(lstDelete.size()>0){
                for (DeleteRequest req : lstDelete) {
                    bulk.add(req);
                }
                lstDelete.clear();
            }
        }

        esClient.bulkAsync(bulk, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse resp) {
                BulkItemResponse[] retArr = resp.getItems();
                for(BulkItemResponse ret:retArr){
                    System.out.println("data_object id=" + ret.getId());
                }
            }

            @Override
            public void onFailure(Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
