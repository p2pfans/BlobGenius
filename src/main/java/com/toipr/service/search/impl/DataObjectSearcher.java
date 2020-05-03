package com.toipr.service.search.impl;

import com.toipr.service.search.SortField;
import com.toipr.service.search.impl.DefaultObjectSearcher;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;

public class DataObjectSearcher extends DefaultObjectSearcher {
    public DataObjectSearcher(String collection){
        super(collection);
    }

    @Override
    protected void buildSort(SearchSourceBuilder builder, SortField sort){
        builder.sort("directory", SortOrder.DESC);
        super.buildSort(builder, sort);
        builder.sort("tmUpload", SortOrder.DESC);
    }
    @Override
    protected void buildSort(SearchSourceBuilder builder, List<SortField> sortList){
        builder.sort("directory", SortOrder.DESC);
        super.buildSort(builder, sortList);
        builder.sort("tmUpload", SortOrder.DESC);
    }
}
