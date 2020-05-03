package com.toipr.util.bean;

import java.util.Map;

/**
 * 对象工厂接口，采用工厂方法设计模式
 */
public interface BeanFactory {
    /**
     * 关闭对象工厂
     */
    void close();

    /**
     * 创建BEAN对象实例
     * @param params 构造函数参数，可以为空
     * @return 新构造对象实例
     */
    Object createObject(Object[] params);

    /**
     * 获取对象属性
     * @param target 对象实例
     * @param fname 属性名称
     * @return 属性值
     */
    Object getProperty(Object target, String fname);

    /**
     * 获取对象target的属性，公共字段与公有get方法的属性
     * @param target 对象实例
     * @param props 属性映射表
     * @return 成功=true 失败=false
     */
    boolean getProperties(Object target, Map<String, Object> props);
    boolean getProperties2(Object target, Map<String, String> props);

    /**
     * 设置BEAN对象成员变量或属性
     * @param target 对象实例
     * @param fname 成员变量或属性名称
     * @param value
     * @return 成功返回true，失败返回false
     */
    boolean setProperty(Object target, String fname, Object value);
    boolean setProperty2(Object target, String fname, String sValue);

    /**
     * 获取成员变量值
     * @param target 对象实例
     * @param fname 字段名称
     * @return 字段值
     */
    Object getFieldValue(Object target, String fname);

    /**
     * 设置成员变量
     * @param target BEAN对象实例
     * @param fname 成员变量名称
     * @param value
     * @return 成功返回true，失败返回false
     */
    boolean setFieldValue(Object target, String fname, Object value);
    boolean setFieldValue2(Object target, String fname, String sValue);

    /**
     * 执行BEAN对象的方法method
     * @param target BEAN对象
     * @param method 方法名称
     * @param args 函数参数
     * @return 返回执行结果
     */
    Object execute(Object target, String method, Object[] args);
}
