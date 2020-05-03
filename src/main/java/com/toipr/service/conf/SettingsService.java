package com.toipr.service.conf;

import com.toipr.model.conf.SysConf;

import java.util.List;

public interface SettingsService {
    /**
     * 获取所有配置项
     * @return 配置项列表
     */
    List<SysConf> getAllConf();

    /**
     * 获取配置项
     * @param name 配置项名称
     * @return
     */
    Object getConf(String name);

    /**
     * 配置项是否存在
     * @param name 配置项名称
     * @return 存在=true
     */
    boolean exists(String name);

    /**
     * 删除配置项
     * @param name 配置名称
     * @return 成功=true 失败=false
     */
    boolean remove(String name);

    /**
     * 添加配置项
     * @param name 配置名称
     * @param type 数据类型
     * @param value 配置值
     * @return 成功=true 失败=false
     */
    boolean addConf(String name, String type, String value);

    /**
     * 根据名称name修改配置项
     * @param name 配置名称
     * @param type 数据类型
     * @param value 配置值
     * @return 成功=true 失败=false
     */
    boolean setConf(String name, String type, String value);
}
