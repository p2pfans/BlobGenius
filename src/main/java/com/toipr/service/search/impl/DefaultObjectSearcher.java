package com.toipr.service.search.impl;

import com.alibaba.fastjson.JSONObject;
import com.toipr.service.search.ObjectSearcher;
import com.toipr.service.search.SortField;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultObjectSearcher implements ObjectSearcher {
    protected List<RestHighLevelClient> queue = new LinkedList<RestHighLevelClient>();

    protected String collection;

    protected String[] hosts;
    protected Object lockObj = new Object();
    public DefaultObjectSearcher(String collection){
        this.collection = collection;
    }

    /**
     * 初始化对象索引器
     * @param hosts 服务器地址
     * @param args 其它参数
     * @return 成功=true 失败=false
     */
    public boolean init(String[] hosts, Object... args){
        this.hosts = hosts;
        return true;
    }

    /**
     * 获取数据对象
     * @param doid 对象ID
     * @return JSONObject实例
     */
    public Object getObject(String doid){
        RestHighLevelClient client = getClient();
        if(client==null){
            return null;
        }

        GetRequest req = new GetRequest(collection, doid);
        try {
            GetResponse resp = client.get(req, RequestOptions.DEFAULT);
            Map<String, Object> objValues = resp.getSource();
            JSONObject json = new JSONObject(objValues);
            backClient(client, false);
            return json;
        } catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return null;
    }

    /**
     * 数据对象doid是否存在
     * @param doid 对象ID
     * @return true=存在
     */
    public boolean exists(String doid){
        RestHighLevelClient client = getClient();
        if(client==null){
            return false;
        }

        GetRequest req = new GetRequest(collection, doid);
        try {
            boolean ret = client.exists(req, RequestOptions.DEFAULT);
            backClient(client, false);
            return ret;
        } catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return false;
    }

    /**
     * 统计匹配条件的记录数量
     * @param params 参数表
     * @return 成功=记录数量 失败=-1
     */
    public int count(Map<String, Object> params){
        RestHighLevelClient client = getClient();
        if(client==null){
            return -1;
        }

        SearchSourceBuilder builder = new SearchSourceBuilder().query(getQuery(params));
        int total = count(client, builder);
        backClient(client, total==-1);
        return total;
    }

    protected int count(RestHighLevelClient client, SearchSourceBuilder builder){
        try {
            CountRequest req = new CountRequest(collection).source(builder);
            CountResponse resp = client.count(req, RequestOptions.DEFAULT);
            return (int)resp.getCount();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * 根据参数查询符合条件的若干条记录
     * @param params 参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param sort 排序字段
     * @return 成功=记录数量 失败=-1
     */
    public int queryObjects(Map<String, Object> params, int start, int count, SortField sort, List rlist){
        SearchSourceBuilder builder = new SearchSourceBuilder().query(getQuery(params));

        return queryObjects(builder, start, count, sort, rlist);
    }

    /**
     * 根据参数查询符合条件的若干条记录
     * @param params 参数表
     * @param start 开始记录
     * @param count 记录数量
     * @param sortList 排序字段列表, 多字段排序
     * @return 成功=记录数量 失败=-1
     */
    public int queryObjects(Map<String, Object> params, int start, int count, List<SortField> sortList, List rlist){
        SearchSourceBuilder builder = new SearchSourceBuilder().query(getQuery(params));

        return queryObjects(builder, start, count, sortList, rlist);
    }

    protected int queryObjects(SearchSourceBuilder builder, int start, int count, Object sorts, List rlist){
        RestHighLevelClient client = getClient();
        if(client==null){
            return -1;
        }

        try {
            int total = count(client, builder);
            if(total<=0){
                backClient(client, false);
                return total;
            }
            if(start>0 || count>0) {
                builder.from(start).size(count);
            }
            if(sorts!=null){
                if(sorts instanceof SortField){
                    buildSort(builder, (SortField)sorts);
                } else {
                    buildSort(builder, (List<SortField>)sorts);
                }
            }

            SearchRequest req = new SearchRequest(collection).source(builder);
            SearchResponse resp = client.search(req, RequestOptions.DEFAULT);
            SearchHits hits = resp.getHits();
            for(SearchHit item : hits){
                JSONObject json = new JSONObject(item.getSourceAsMap());
                rlist.add(json);
            }
            backClient(client, false);
            return total;
        } catch(Exception ex){
            ex.printStackTrace();
            backClient(client, true);
        }
        return -1;
    }

    protected QueryBuilder getQuery(Map<String, Object> params){
        if(params.size()==0){
            return QueryBuilders.matchAllQuery();
        }

        BoolQueryBuilder qobj = QueryBuilders.boolQuery();
        for(Map.Entry<String, Object> item : params.entrySet()){
            qobj.must(QueryBuilders.matchQuery(item.getKey(), item.getValue()));
        }
        return qobj;
    }

    protected void buildSort(SearchSourceBuilder builder, SortField sort){
        if(sort!=null) {
            builder.sort(sort.field, sort.desc ? SortOrder.DESC : SortOrder.ASC);
        }
    }
    protected void buildSort(SearchSourceBuilder builder, List<SortField> sortList){
        if(sortList!=null && sortList.size()>0) {
            for(SortField sort:sortList) {
                builder.sort(sort.field, sort.desc ? SortOrder.DESC : SortOrder.ASC);
            }
        }
    }

    protected RestHighLevelClient getClient(){
        synchronized (lockObj) {
            if (queue.size() > 0) {
                return queue.remove(0);
            }

            HttpHost[] httpHosts = new HttpHost[hosts.length];
            for (int i = 0; i < hosts.length; i++) {
                httpHosts[i] = HttpHost.create(hosts[i]);
            }

            RestClientBuilder builder = RestClient.builder(httpHosts);
            RestHighLevelClient client = new RestHighLevelClient(builder);
            return client;
        }
    }

    protected void backClient(RestHighLevelClient client, boolean isClose){
        if(isClose){
            try{
                client.close();
            }catch(Exception ex){
                ;
            }
            return;
        }

        synchronized (lockObj){
            queue.add(client);
        }
    }
}
