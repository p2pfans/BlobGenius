package com.toipr.service.search;

import com.toipr.service.search.impl.DataObjectIndexer;
import com.toipr.service.search.impl.DataObjectSearcher;
import com.toipr.service.search.impl.DefaultNodeManager;
import com.toipr.service.search.impl.DefaultObjectSearcher;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class SearchServices {
    protected static NodeManager instance = null;

    public static synchronized boolean createManager(ApplicationContext context, String[] mappers){
        if(instance==null){
            DefaultNodeManager myobj = new DefaultNodeManager(context);
            if(!myobj.init(mappers)){
                return false;
            }
            instance = myobj;
        }
        return true;
    }
    public static NodeManager getManager(){
        return instance;
    }

    public static ObjectIndexer getIndexer(String resid){
        return instance.getIndexer(resid);
    }

    public static ObjectSearcher getSearcher(String resid){
        return instance.getSearcher(resid);
    }
}
