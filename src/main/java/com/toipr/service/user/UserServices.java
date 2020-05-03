package com.toipr.service.user;

import com.toipr.service.user.impl.DefaultUserService;
import org.springframework.context.ApplicationContext;

public class UserServices {
    protected static DefaultUserService instance = null;
    public static synchronized boolean createInstance(ApplicationContext context, String[] mappers){
        if(instance==null){
            DefaultUserService temp = new DefaultUserService(context);
            if(!temp.init(mappers)){
                return false;
            }
            instance = temp;
        }
        return true;
    }

    public static UserService getInstance(){
        return instance;
    }
}
