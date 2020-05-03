package com.toipr.service;

import com.toipr.conf.MybatisConfig;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

public abstract class DefaultService {
    protected ApplicationContext context;
    protected MybatisConfig mybatis;

    public DefaultService(ApplicationContext context){
        this.context = context;
    }

    public boolean init(String[] mappers){
        DataSource ds = (DataSource)context.getBean("dataSource");
        if(ds==null){
            return false;
        }

        mybatis = new MybatisConfig(ds);
        if(!mybatis.init(mappers)){
            return false;
        }
        return true;
    }

    public void addMappers(String type, String[] mappers){
    }
}
