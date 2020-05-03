package com.toipr.util.bean.impl;

import com.toipr.util.bean.BeanFactory;

import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缺省BEAN对象工厂
 * @author 明月照我行@简书
 */
public class DefaultBeanFactory implements BeanFactory {
    /**
     * 构造函数、方法与成员变量与简单签名哈希表缓存，减少反射函数调用、提升性能
     */
    private static Map<String, Object> mapCache = new ConcurrentHashMap<String, Object>();

    /**
     * 类全限定名称
     */
    private String clazzName;
    /**
     * 对象工厂生成的类型
     */
    private Class<?> clazz;

    public DefaultBeanFactory(Class<?> clazz){
        this.clazz = clazz;
        this.clazzName = clazz.getName() + ":";
    }

    /**
     * 关闭对象工厂
     */
    public void close(){
        /**
         * 清除以本类型名称开头的缓存
         */
        for(String key : mapCache.keySet()){
            if(key.startsWith(this.clazzName)){
                mapCache.remove(key);
            }
        }
        this.clazz = null;
    }

    /**
     * 创建BEAN对象实例
     * @param params 构造函数参数，可以为空
     * @return 实例对象
     */
    public Object createObject(Object[] params) {
        try {
            Class<?>[] carr = null;
            if (params != null && params.length > 0) {
                /**
                 * 初始化参数类型数组
                 */
                carr = new Class<?>[params.length];
                for (int i = 0; i < params.length; i++) {
                    carr[i] = params[i].getClass();
                }
            }
            if (carr == null || carr.length==0) {
                /**
                 * 用缺省构造函数初始化类对象
                 */
                return clazz.newInstance();
            }

            /**
             * 根据参数个数与类型获取合适的构造函数
             */
            Constructor cons = getConstructor(carr);
            if (cons == null) {
                return null;
            }
            /**
             * 调用构造函数，创建BEAN对象实例
             */
            return cons.newInstance(params);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 在target对象指向method方法
     * @param target BEAN对象
     * @param method 方法名称
     * @param args 函数参数
     * @return
     */
    public Object execute(Object target, String method, Object[] args){
        try {
            Class<?>[] pTypes = null;
            if (args == null || args.length == 0) {
                pTypes = new Class<?>[0];
            } else {
                pTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    pTypes[i] = args[i].getClass();
                }
            }

            /**
             * 根据方法名称、参数个数与参数类型匹配最合适方法对象
             */
            Method mobj = getObjectMethod(method, pTypes);
            if (mobj == null) {
                return null;
            }
            /**
             * 执行方法调用
             */
            return mobj.invoke(target, args);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 根据参数列表获取对象的构造函数
     * @param paramTypes
     * @return
     */
    protected Constructor getConstructor(Class<?>[] paramTypes){
        String sKey = null;
        StringBuilder str = new StringBuilder(256);
        str.append(this.clazzName);
        str.append("constructor:");
        for (int i = 0; i < paramTypes.length; i++) {
            appendParamType(paramTypes[i], str);
        }
        sKey = str.toString();

        /**
         * 检查是否存在构造函数缓存
         */
        if(mapCache.containsKey(sKey)){
            return (Constructor)mapCache.get(sKey);
        }

        Constructor cons = null;
        try{
            /**
             * 直接获取最匹配的构造函数
             */
            cons = clazz.getDeclaredConstructor(paramTypes);
        } catch(Exception ex){
            ex.printStackTrace();
        }

        if(cons==null){
            /**
             * 查找参数类型等同的构造函数
             */
            Constructor[] carr = clazz.getDeclaredConstructors();
            for(int i=0; i<carr.length; i++){
                if(isRightExecutable(carr[i], paramTypes)){
                    cons = carr[i];
                    break;
                }
            }
        }

        if(cons!=null){
            /**
             * 设置访问权限，设置构造函数缓存
             */
            if(!Modifier.isPublic(cons.getModifiers())){
                cons.setAccessible(true);
            }
            mapCache.put(sKey, cons);
        }
        return cons;
    }

    /**
     * 判断Method或Constructor的参数类型是否与paramTypes匹配
     * 处理参数类型细微差异导致的匹配异常，如byte与Byte
     * @param exec
     * @param paramTypes
     * @return 匹配返回true
     */
    public boolean isRightExecutable(Executable exec, Class<?>[] paramTypes){
        Class<?>[] carr = exec.getParameterTypes();
        if(carr.length!=paramTypes.length){
            return false;
        }

        for(int i=0; i<carr.length; i++){
            if(!isTypeSame(paramTypes[i], carr[i])){
                return false;
            }
        }
        return true;
    }

    /**
     * 获取对象属性
     * @param target 对象实例
     * @param fname 属性名称
     * @return 属性值
     */
    public Object getProperty(Object target, String fname){
        try {
            Object objValue = getPropertyValue(target, fname);
            if(objValue!=null) {
                return objValue;
            }
            return getFieldValue(target, fname);
        }catch(Exception ex){
            ;
        }
        return null;
    }

    /**
     * 获取对象target的属性，公共字段与公有get方法的属性
     * @param target 对象实例
     * @param props 属性映射表
     * @return 成功=true 失败=false
     */
    public boolean getProperties(Object target, Map<String, Object> props){
        try{
            Field[] farr = clazz.getDeclaredFields();
            for(int i=0; i<farr.length; i++){
                Field fobj = farr[i];

                Object ret = null;
                if(Modifier.isPublic(fobj.getModifiers())){
                    ret = fobj.get(target);
                } else {
                    ret = getPropertyValue(target, fobj.getName());
                }
                if(ret!=null) {
                    props.put(fobj.getName(), ret);
                }
            }
            return true;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public boolean getProperties2(Object target, Map<String, String> props){
        try{
            Field[] farr = clazz.getDeclaredFields();
            for(int i=0; i<farr.length; i++){
                Field fobj = farr[i];

                Object ret = null;
                if(Modifier.isPublic(fobj.getModifiers())){
                    ret = fobj.get(target);
                } else {
                    ret = getPropertyValue(target, fobj.getName());
                }
                if(ret!=null) {
                    Class<?> fcls = fobj.getType();
                    String type = fcls.getSimpleName();
                    if(type.compareToIgnoreCase("date")==0){
                        Date dtNow = (Date)ret;
                        String str = String.format("%04d-%02d-%02d %02d:%02d:%02d", dtNow.getYear() + 1900,
                                dtNow.getMonth(), dtNow.getDay(), dtNow.getHours(), dtNow.getMinutes(), dtNow.getSeconds());
                        props.put(fobj.getName(), str);
                    } else {
                        props.put(fobj.getName(), ret.toString());
                    }
                }
            }
            return true;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 设置对象的属性或成员变量，优先设置属性，然后尝试成员变量
     * @param target 对象实例
     * @param fname 成员变量或属性名称
     * @param value
     * @return
     */
    public boolean setProperty(Object target, String fname, Object value) {
        try {
            /**
             * 优先设置属性，然后尝试成员变量，这种逻辑可能存在问题
             */
            if (setProperyValue(target, fname, value)) {
                return true;
            }
            return setFieldValue(target, fname, value);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public boolean setProperty2(Object target, String fname, String sValue) {
        try{
            Field fobj = getObjectField(fname);
            Class<?> fclazz = fobj.getType();
            String sType = fclazz.getSimpleName();
            if(fclazz.isPrimitive()){
            }

            Object objValue = getObjectValue(sValue, sType);
            if(!setProperyValue(target, fname, objValue)){
                fobj.set(target, objValue);
            }
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 获取成员变量值
     * @param target 对象实例
     * @param fname 字段名称
     * @return 字段值
     */
    public Object getFieldValue(Object target, String fname){
        try {
            Field field = getObjectField(fname);
            if (field != null) {
                return field.get(target);
            }
        } catch(Exception ex){
            ;
        }
        return null;
    }

    /**
     * 设置BEAN对象的成员变量
     * @param target BEAN对象实例
     * @param fname 成员变量名称
     * @param value
     * @return true=执行成功
     */
    public boolean setFieldValue(Object target, String fname, Object value){
        Field field = getObjectField(fname);
        if(field==null){
            return false;
        }
        try {
            field.set(target, value);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public boolean setFieldValue2(Object target, String fname, String sValue){
        return false;
    }

    /**
     * 根据名称获取成员变量
     * @param fname
     * @return
     */
    protected Field getObjectField(String fname){
        Field field = null;
        /**
         * 检查是否存在缓存字段
         */
        String sKey = this.clazzName + fname;
        if(mapCache.containsKey(sKey)){
            return (Field)mapCache.get(sKey);
        }

        try{
            /**
             * 获取申明相同的成员字段
             */
            field = clazz.getDeclaredField(fname);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        if(field==null){
            /**
             * 字段名称执行Camel命名规范后重新获取
             */
            String fname2 = toCamelStyleName(fname);
            if(fname2.compareTo(fname)!=0) {
                try {
                    field = clazz.getDeclaredField(fname2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if(field!=null){
            /**
             * 设置访问权限，设置字段缓存
             */
            if(!Modifier.isPublic(field.getModifiers())){
                field.setAccessible(true);
            }
            mapCache.put(sKey, field);
        }
        return field;
    }

    protected Object getPropertyValue(Object target, String fname){
        try{
            /**
             * 属性名称自动转换为驼峰格式命名规范，解决userName与user_name等差异
             */
            String field = toCamelStyleName(fname);
            field = firstCapital(field);

            Class<?>[] clsArr = null;
            Method method = getObjectMethod("get" + field, clsArr);
            if(method==null){
                return false;
            }
            return method.invoke(target);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 设置对象属性值
     * @param target 对象实例
     * @param fname 属性名称，属性自动转换为驼峰命名规范
     * @param value 属性值
     * @return true=成功执行
     */
    protected boolean setProperyValue(Object target, String fname, Object value){
        try{
            /**
             * 属性名称自动转换为驼峰格式命名规范，解决userName与user_name等差异
             */
            String field = toCamelStyleName(fname);
            field = firstCapital(field);

            Method method = getObjectMethod("set" + field, value.getClass());
            if(method==null){
                return false;
            }
            method.invoke(target, value);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public boolean setProperty2(Object target, String fname, Object value){
        return false;
    }

    /**
     * 判断类方法的参数类型是否与pType匹配
     * @param exec
     * @param pType
     * @return 匹配返回true
     */
    protected boolean isRightMethod(Executable exec, Class<?> pType){
        Class<?>[] carr = exec.getParameterTypes();
        if(carr.length!=1){
            return false;
        }
        return isTypeSame(pType, carr[0]);
    }

    /**
     * 比较两个类型是否相等，或者src是target的子类或接口实现
     * @param src
     * @param target
     * @return true=相等或子类型
     */
    protected boolean isTypeSame(Class<?> src, Class<?> target){
        /**
         * 两个类型是否相等
         */
        if(src.equals(target)){
            return true;
        }
        /**
         * src是否为target的子类型
         */
        if(src.isAssignableFrom(target)){
            return true;
        }

        /**
         * 解决基本类型等同问题，例如：byte与java.lang.Byte，short与java.lang.Short
         */
        String name1 = target.getName().toLowerCase();
        int pos = name1.lastIndexOf('.');
        if(pos>0){
            name1 = name1.substring(pos+1);
        }

        String name2 = src.getName().toLowerCase();
        pos = name2.lastIndexOf('.');
        if(pos>0){
            name2 = name2.substring(pos+1);
        }
        if(name1.compareTo(name2)==0){
            return true;
        }
        if(name1.compareTo("int")==0 || name1.compareTo("integer")==0){
            if(name2.compareTo("int")==0 || name2.compareTo("integer")==0){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取BEAN对象的名称为name、参数类型匹配pTypes的函数
     * @param name
     * @param pTypes
     * @return
     */
    public Method getObjectMethod(String name, Class<?>[] pTypes){
        String sKey = getMethodKey(name, pTypes);
        /**
         * 检查是否存在方法缓存
         */
        if(mapCache.containsKey(sKey)){
            return (Method)mapCache.get(sKey);
        }

        Method method = null;
        try{
            /**
             * 获取相同签名的方法
             */
            method = clazz.getDeclaredMethod(name, pTypes);
        }catch(Exception ex){
        }

        //TODO 还有一个问题，静态方法如何调用
        if(method==null){
            /**
             * 匹配公有方法
             */
            method = getObjectMethod(name, true, pTypes);
        }
        if(method==null){
            /**
             * 匹配所有方法
             */
            method = getObjectMethod(name, false, pTypes);
        }

        if(method!=null){
            /**
             * 设置访问权限，设置方法缓存
             */
            if(!Modifier.isPublic(method.getModifiers())){
                method.setAccessible(true);
            }
            mapCache.put(sKey, method);
        }
        return method;
    }

    protected Method getObjectMethod(String name, boolean isPublic, Class<?>[] pTypes){
        Method method = null;
        Method[] marr = null;
        if(isPublic) {
            /**
             * 获取公有方法
             */
            marr = clazz.getMethods();
        } else {
            /**
             * 获取所有方法
             */
            marr = clazz.getDeclaredMethods();
        }

        for(int i=0; i<marr.length; i++){
            Method mobj = marr[i];
            String func = mobj.getName();
            if(func.compareTo(name)!=0){
                continue;
            }
            if(isRightExecutable(mobj, pTypes)){
                method = mobj;
                break;
            }
        }
        return method;
    }

    /**
     * 获取BEAN对象的名称为name的、参数只有一个pType类型的函数
     * @param name
     * @param pType
     * @return
     */
    public Method getObjectMethod(String name, Class<?> pType){
        String sKey = getMethodKey(name, pType);
        /**
         * 检查是否有方法缓存
         */
        if(mapCache.containsKey(sKey)){
           return (Method)mapCache.get(sKey);
        }

        Method method = null;
        try{
            /**
             * 检查是否存在直接匹配的签名方法
             */
            method = clazz.getDeclaredMethod(name, pType);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        if(method==null){
            /**
             * 检查是否存在名称相同、参数类型等同的方法
             */
            Method[] marr = clazz.getMethods();
            for(int i=0; i<marr.length; i++){
                Method mobj = marr[i];
                String name2 = mobj.getName();
                if(name.compareToIgnoreCase(name2)==0){
                    /**
                     * 名称相同的情况下，检查参数类型是否相等或等同
                     */
                    if(isRightMethod(mobj, pType)){
                        method = mobj;
                        break;
                    }
                }
            }
        }
        if(method!=null){
            /**
             * 修改访问权限，设置方法缓存
             */
            if(!Modifier.isPublic(method.getModifiers())){
                method.setAccessible(true);
            }
            mapCache.put(sKey, method);
        }
        return method;
    }

    /**
     * 获取方法简单签名，参数只有一个
     * @param name
     * @param pType
     * @return
     */
    protected String getMethodKey(String name, Class<?> pType){
        StringBuilder str = new StringBuilder(256);
        str.append(this.clazzName);
        str.append(name + ":");
        if(pType!=null){
            appendParamType(pType, str);
        }
        return str.toString();
    }

    /**
     * 获取方法简单签名，参数为一个或多个
     * @param name
     * @param pTypes
     * @return
     */
    protected String getMethodKey(String name, Class<?>[] pTypes){
        StringBuilder str = new StringBuilder(256);
        str.append(this.clazzName);
        str.append(name + ":");
        if(pTypes!=null && pTypes.length>0) {
            for (int i = 0; i < pTypes.length; i++) {
                appendParamType(pTypes[i], str);
            }
        }
        return str.toString();
    }

    protected void appendParamType(Class<?> pType, StringBuilder sType){
        String name = pType.getName();
        int pos = name.lastIndexOf('.');
        if(pos>0){
            name = name.substring(pos+1);
        }
        sType.append(name + ":");
    }

    /**
     * 将文本text的第一个字母转换成大写
     * @param text
     * @return
     */
    public static String firstCapital(String text){
        if(text.length()>1) {
            return (text.substring(0, 1).toUpperCase() + text.substring(1));
        }
        if(text.length()==1){
            return text.toUpperCase();
        }
        return text;
    }

    /**
     * 将文本text转换成符合驼峰格式
     * @param text
     * @return
     */
    public static String toCamelStyleName(String text){
        text = toCamelStyleName(text, "-");
        text = toCamelStyleName(text, "_");
        text = toCamelStyleName(text, ".");
        return text;
    }

    protected static String toCamelStyleName(String text, String sep){
        int len = sep.length();
        int pos = text.indexOf(sep);
        while(pos>0){
            String left = text.substring(0, pos);
            String right = text.substring(pos + len);
            if(right.length()>0){
                right = firstCapital(right);
            }
            text = left + right;
            pos = text.indexOf(sep, pos + len);
        }
        return text;
    }

    /**
     * 将字符串转换成基本类型的值
     * @param sValue
     * @param sType
     * @return
     */
    public Object getObjectValue(String sValue, String sType) throws Exception {
        if(sValue!=null && sValue.compareTo("null")==0){
            return null;
        }
        if(sType==null || sType.length()==0){
            return sValue;
        }

        sType = sType.toLowerCase();
        if(sType.compareTo("string")==0){
            return sValue;
        }

        if(sType.compareTo("byte")==0){
            return Byte.parseByte(sValue);
        }
        if(sType.compareTo("char")==0){
            return sValue.charAt(0);
        }
        if(sType.compareTo("short")==0){
            return Short.parseShort(sValue);
        }
        if(sType.compareTo("int")==0 || sType.compareTo("integer")==0){
            return Integer.parseInt(sValue);
        }
        if(sType.compareTo("long")==0){
            return Long.parseLong(sValue);
        }
        if(sType.compareTo("float")==0){
            return Float.parseFloat(sValue);
        }
        if(sType.compareTo("double")==0){
            return Double.parseDouble(sValue);
        }
        if(sType.compareTo("datetime")==0 || sType.compareTo("date")==0){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return sdf.parse(sValue);
        }
        return sValue;
    }
}
