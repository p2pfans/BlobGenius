package com.toipr.service.server;

import com.toipr.service.server.impl.DefaultDataNodeRouter;
import org.springframework.context.ApplicationContext;

public class DataServers {
    protected static DataNodeRouter instance = null;

    public static synchronized boolean createInstance(ApplicationContext ctx, String[] mappers){
        if(instance==null) {
            DefaultDataNodeRouter myobj = new DefaultDataNodeRouter(ctx);
            if(!myobj.init(mappers)){
                return false;
            }
            instance = myobj;
        }
        return true;
    }

    public static DataNodeRouter getInstance(){
        return instance;
    }
}
