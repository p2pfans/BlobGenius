package com.toipr.service.org;

import com.toipr.service.org.impl.DefaultOrgService;
import org.springframework.context.ApplicationContext;

public class OrgServices {
    protected static OrgService instance = null;
    public static synchronized boolean createInstance(ApplicationContext context, String[] mappers){
        if(instance==null){
            DefaultOrgService service = new DefaultOrgService(context);
            if(!service.init(mappers)){
                return false;
            }
            instance = service;
        }
        return true;
    }

    public static OrgService getInstance(){
        return instance;
    }
}
