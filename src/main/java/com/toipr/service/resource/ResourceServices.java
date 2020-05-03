package com.toipr.service.resource;

import com.toipr.service.resource.impl.DefaultResourceService;
import org.springframework.context.ApplicationContext;

public class ResourceServices {
    protected static ResourceService instance = null;
    public static synchronized boolean createInstance(ApplicationContext context, String[] mappers){
        if(instance==null){
            DefaultResourceService temp = new DefaultResourceService(context);
            if(!temp.init(mappers)){
                return false;
            }
            instance = temp;
        }
        return true;
    }

    public static ResourceService getInstance(){
        return instance;
    }

    public static RuleService getRuleService() {
        return (RuleService)instance;
    }
}
