package com.toipr.mapper.user;

import com.toipr.model.user.CustomData;

import java.util.List;

public interface CustomDataMapper {
    /**
     * 添加自定义数据项
     * @param item 数据对象
     * @param table 数据表名称
     * @return 成功返回1
     */
    int addData(CustomData item, String table);

    /**
     * 设置数据项
     * @param oid 拥有者ID
     * @param field 属性名
     * @param value 属性值
     * @param table 数据表名
     * @return 成功返回1
     */
    int setData(String oid, String field, String value, String table);

    /**
     * 删除拥有者的数据项, field为空删除所有
     * @param oid 拥有者ID
     * @param field 属性名
     * @param table 数据表名
     * @return 成功返回大于1的值
     */
    int removeData(String oid, String field, String table);

    /**
     * 获取自定义数据项
     * @param oid 拥有者ID
     * @param field 属性名
     * @param table 数据表名
     * @return 自定义数据对象
     */
    CustomData getData(String oid, String field, String table);

    /**
     * 获取自定义数据项文本串
     * @param oid 拥有者ID
     * @param field 属性名
     * @param table 数据表名
     * @return 成功自定义数据项文本串
     */
    String getDataString(String oid, String field, String table);

    /**
     * 获取拥有者的所有数据项列表
     * @param oid 拥有者ID
     * @param table 数据表名
     * @return 数据项列表
     */
    List<CustomData> getAllData(String oid, String table);
}
