package com.toipr.service.data;

import com.toipr.service.data.impl.FileBlobStore;
import com.toipr.socket.client.NetBlobStore;

import java.util.HashMap;
import java.util.Map;

public class BlobStores {
    protected static Map<String, BlobStore> mapStore = new HashMap<String, BlobStore>();
    public static synchronized boolean createBlobStore(String rid, String key, boolean isLocal, Object... args){
        if(mapStore.containsKey(key)){
            return true;
        }

        BlobStore myobj = null;
        if(isLocal) {
            myobj = new FileBlobStore();
        } else {
            myobj = new NetBlobStore();
        }
        if(!myobj.init(rid, args)){
            return false;
        }
        mapStore.put(key, myobj);
        return true;
    }

    public static BlobStore getBlobStore(String key){
        if(mapStore.containsKey(key)){
            return mapStore.get(key);
        }
        return null;
    }
}
