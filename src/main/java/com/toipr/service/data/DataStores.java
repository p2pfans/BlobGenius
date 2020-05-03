package com.toipr.service.data;

import com.toipr.service.data.impl.DefaultDataStoreService;

public class DataStores {
    protected static DataStoreService instance = null;
    public static DataStoreService getInstance(){
        if(instance==null){
            synchronized (DataStoreService.class){
                if(instance==null){
                    instance = new DefaultDataStoreService();
                }
            }
        }
        return instance;
    }
}
