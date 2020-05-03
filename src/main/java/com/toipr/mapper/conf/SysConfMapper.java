package com.toipr.mapper.conf;

import com.toipr.model.conf.SysConf;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysConfMapper {
    /**
     * 获取所有配置项
     * @return 配置项列表
     */
    List<SysConf> getAllConf();

    /**
     * 添加配置项
     * @param name 配置名称
     * @param type 数据类型
     * @param value 配置值
     * @return 成功=1 失败=0
     */
    int addConf(@Param("name") String name, @Param("type") String type, @Param("value") String value);

    /**
     * 配置项是否存在
     * @param name 配置项名称
     * @return 存在=1
     */
    int exists(@Param("name") String name);

    /**
     * 删除配置项
     * @param name 配置名称
     * @return 成功=1 失败=0
     */
    int remove(@Param("name") String name);

    /**
     * 修改配置项
     * @param name 配置名称
     * @param type 数据类型
     * @param value 配置值
     * @return 成功=1 失败=0
     */
    int setConf(@Param("name") String name, @Param("type") String type, @Param("value") String value);
}
