package com.toipr.service.search;

import com.toipr.service.search.impl.DataObjectIndexer;
import com.toipr.service.search.impl.DataObjectSearcher;
import com.toipr.service.search.impl.DefaultObjectSearcher;

import java.util.HashMap;
import java.util.Map;

public class SearchServices {
    protected static Map<String, ObjectIndexer> mapIndexer = new HashMap<String, ObjectIndexer>();
    protected static Map<String, ObjectSearcher> mapSearcher = new HashMap<String, ObjectSearcher>();

    public static synchronized boolean createIndexer(String channel, String collection, String[] hosts, Object... args){
        if(mapIndexer.containsKey(channel)){
            return true;
        }

        if(channel.compareToIgnoreCase("objects")==0){
            DataObjectIndexer indexer = new DataObjectIndexer(channel, collection);
            if(!indexer.init(hosts, args)){
                return false;
            }
            mapIndexer.put(channel, indexer);
        }
        return true;
    }

    public static ObjectIndexer getIndexer(String channel){
        if(mapIndexer.containsKey(channel)){
            return mapIndexer.get(channel);
        }
        return null;
    }

    public static synchronized boolean createSearcher(String collection, String[] hosts, Object... args){
        if(mapSearcher.containsKey(collection)){
            return true;
        }

        ObjectSearcher myobj = null;
        if(collection.compareTo("data_objects")==0){
            myobj = new DataObjectSearcher(collection);
        } else {
            myobj = new DefaultObjectSearcher(collection);
        }
        if(!myobj.init(hosts, args)){
            return false;
        }
        mapSearcher.put(collection, myobj);
        return true;
    }

    public static ObjectSearcher getSearcher(String collection){
        if(mapSearcher.containsKey(collection)){
            return mapSearcher.get(collection);
        }
        return null;
    }
}
