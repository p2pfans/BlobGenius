package com.toipr.service.conf.impl;

import com.toipr.conf.MySettings;
import com.toipr.mapper.conf.SysConfMapper;
import com.toipr.model.conf.SysConf;
import com.toipr.service.DefaultService;
import com.toipr.service.conf.SettingsService;
import com.toipr.util.Utils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class MySettingsService extends DefaultService implements SettingsService {
    protected MySettings settings;
    public MySettingsService(ApplicationContext context){
        super(context);
    }

    @Override
    public boolean init(String[] mappers) {
        if(!super.init(mappers)){
            return false;
        }

        settings = (MySettings)context.getBean("mysettings");
        if(settings==null){
            return false;
        }
        getAllConf();
        return true;
    }

    /**
     * 获取所有配置项
     * @return 配置项列表
     */
    public List<SysConf> getAllConf(){
        try(SqlSession session=mybatis.getSession()){
            SysConfMapper mapper = session.getMapper(SysConfMapper.class);
            List<SysConf> plist = mapper.getAllConf();
            if(plist!=null && plist.size()>0){
                for(SysConf item : plist){
                    Object objValue = getValue(item.getType(), item.getValue());
                    if(objValue!=null){
                        settings.setProperty(item.getName(), objValue);
                    }
                }
            }
            return plist;
        }
    }

    /**
     * 获取配置项
     * @param name 配置项名称
     * @return
     */
    public Object getConf(String name){
        return settings.getProperty(name);
    }

    /**
     * 配置项是否存在
     * @param name 配置项名称
     * @return 存在=true
     */
    public boolean exists(String name){
        if(settings.hasProperty(name)){
            return true;
        }

        try(SqlSession session=mybatis.getSession()){
            SysConfMapper mapper = session.getMapper(SysConfMapper.class);
            return mapper.exists(name)>0;
        }
    }

    /**
     * 删除配置项
     * @param name 配置名称
     * @return 成功=true 失败=false
     */
    public boolean remove(String name){
        settings.removeProperty(name);
        try(SqlSession session=mybatis.getSession()){
            SysConfMapper mapper = session.getMapper(SysConfMapper.class);
            int ret = mapper.remove(name);
            if(ret>0){
                session.commit();
                settings.removeProperty(name);
            }
            return (ret>0);
        }
    }

    /**
     * 添加配置项
     * @param name 配置名称
     * @param type 数据类型
     * @param value 配置值
     * @return 成功=true 失败=false
     */
    public boolean addConf(String name, String type, String value){
        try(SqlSession session=mybatis.getSession()){
            SysConfMapper mapper = session.getMapper(SysConfMapper.class);
            int ret = mapper.addConf(name, type, value);
            if(ret>0){
                session.commit();

                Object objValue = getValue(type, value);
                if(objValue!=null){
                    settings.setProperty(name, objValue);
                }
            }
            return ret>0;
        }
    }

    /**
     * 根据名称name修改配置项
     * @param name 配置名称
     * @param type 数据类型
     * @param value 配置值
     * @return 成功=true 失败=false
     */
    public boolean setConf(String name, String type, String value){
        try(SqlSession session=mybatis.getSession()){
            SysConfMapper mapper = session.getMapper(SysConfMapper.class);
            int ret = mapper.setConf(name, type, value);
            if(ret>0){
                session.commit();

                Object objValue = getValue(type, value);
                if(objValue!=null){
                    settings.setProperty(name, objValue);
                }
            }
            return ret>0;
        }
    }

    protected Object getValue(String type, String value){
        if(Utils.isNullOrEmpty(type)){
            return value;
        }

        type = type.toLowerCase();
        try {
            switch (type) {
                case "boolean":
                    return Boolean.parseBoolean(value);
                case "byte":
                    return Byte.parseByte(value);
                case "short":
                    return Short.parseShort(value);
                case "int":
                    return Integer.parseInt(value);
                case "long":
                    return Long.parseLong(value);
                case "float":
                    return Float.parseFloat(value);
                case "double":
                    return Double.parseDouble(value);
                default:
                    return value;
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
