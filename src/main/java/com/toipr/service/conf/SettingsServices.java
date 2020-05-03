package com.toipr.service.conf;

import com.toipr.service.conf.impl.MySettingsService;
import org.springframework.context.ApplicationContext;

public class SettingsServices {
    protected static SettingsService instance = null;
    public static synchronized boolean createInstance(ApplicationContext context, String... mappers){
        if(instance==null){
            MySettingsService myobj = new MySettingsService(context);
            if(!myobj.init(mappers)){
                return false;
            }
            instance = myobj;
        }
        return true;
    }

    public static SettingsService getInstance(){
        return instance;
    }
}
