package com.toipr.util.bean;

import com.toipr.util.bean.impl.DefaultBeanFactory;

/**
 * 静态辅助类，生成加载器与对象工厂
 */
public class Beans {
    /**
     * 生成指定类型的对象工厂
     * 对象工厂采用反射技术，实现3个功能:
     * 1.调用构造函数生成对象
     * 2.设置对象成员变量或属性
     * 3.根据设定的函数名称与参数列表执行对象函数
     * @param clazz
     * @return 对象工厂实例
     */
    public static BeanFactory newBeanFactory(Class<?> clazz){
        DefaultBeanFactory obj = new DefaultBeanFactory(clazz);
        return obj;
    }
}
