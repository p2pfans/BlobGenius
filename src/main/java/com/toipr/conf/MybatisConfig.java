package com.toipr.conf;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisConfig {
    protected static Map<String, MybatisConfig> mapMybatis = new HashMap<String, MybatisConfig>();
    public static synchronized MybatisConfig getMybatis(String name, String[] mappers, DataSource source){
        if(mapMybatis.containsKey(name)){
            return mapMybatis.get(name);
        }

        MybatisConfig mybatis = new MybatisConfig(source);
        if(!mybatis.init(mappers)){
            return null;
        }
        mapMybatis.put(name, mybatis);
        return mybatis;
    }

    private DataSource dataSource;
    private SqlSessionFactory sessionFactory;

    public MybatisConfig(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public SqlSession getSession(){
        return sessionFactory.openSession();
    }

    public boolean init(String[] mappers){
        SqlSessionFactoryBean bean=new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        ResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();
        try {
//            bean.setMapperLocations(resolver.getResources("classpath*:mapper/**.xml"));
            List<Resource> rlist = new ArrayList<Resource>();
            for(int i=0; i<mappers.length; i++){
                Resource[] rarr = resolver.getResources(mappers[i]);
                for(int t=0; t<rarr.length; t++) {
                    rlist.add(rarr[t]);
                }
            }

            Resource[] myarr = new Resource[rlist.size()];
            for(int i=0; i<rlist.size(); i++){
                myarr[i] = rlist.get(i);
            }
            bean.setMapperLocations(myarr);
            sessionFactory = bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }
}
