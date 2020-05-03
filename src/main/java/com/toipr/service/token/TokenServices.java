package com.toipr.service.token;

import com.toipr.service.token.impl.DefaultTokenService;

public class TokenServices {
    protected static TokenService instance = null;
    public static synchronized boolean createInstance(String dbname, Object...args){
        if(instance==null){
            DefaultTokenService myobj = new DefaultTokenService();
            if(!myobj.init(dbname, args)){
                return false;
            }
            instance = myobj;
        }
        return true;
    }

    public static TokenService getInstance(){
        return instance;
    }
}
