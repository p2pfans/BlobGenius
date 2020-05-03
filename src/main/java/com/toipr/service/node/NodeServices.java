package com.toipr.service.node;

import com.toipr.service.node.impl.DefaultNodeService;
import org.springframework.context.ApplicationContext;

public class NodeServices {
    protected static NodeService instance = null;
    public static synchronized boolean createInstance(ApplicationContext context, String[] mappers){
        if(instance==null){
            DefaultNodeService myobj = new DefaultNodeService(context);
            if(!myobj.init(mappers)){
                return false;
            }
            instance = myobj;
        }
        return true;
    }

    public static NodeService getInstance(){
        return instance;
    }
}
