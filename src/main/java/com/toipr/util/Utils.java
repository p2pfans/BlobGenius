package com.toipr.util;

import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class Utils {
    /**
     * 判断文本text是否为空
     * @param text
     * @return true=null或空 false=不为空
     */
    public static boolean isNullOrEmpty(String text){
        if(text==null || text.length()==0){
            return true;
        }
        return false;
    }

    /**
     * 根据属性名称与类型从JSON对象获取属性值
     * @param name 属性名称
     * @param clazz 属性类型
     * @param obj JSON对象
     * @return 属性值
     */
    public static Object getObject(String name, Class<?> clazz, JSONObject obj){
        if(!obj.containsKey(name)){
            return null;
        }

        if(clazz.isAssignableFrom(String.class)){
            String sValue = obj.getString(name);
            if(isNullOrEmpty(sValue)){
                return null;
            }
            return sValue;
        }

        if(clazz.isAssignableFrom(Date.class)){
            return obj.getDate(name);
        }
        if(clazz.isAssignableFrom(BigInteger.class)){
            return obj.getBigInteger(name);
        }
        if(clazz.isAssignableFrom(BigDecimal.class)){
            return obj.getBigDecimal(name);
        }

        if(clazz==int.class || clazz.isAssignableFrom(Integer.class)){
            return obj.getIntValue(name);
        }

        if(clazz==boolean.class || clazz.isAssignableFrom(Boolean.class)){
            return obj.getBooleanValue(name);
        }
        if(clazz==byte.class || clazz.isAssignableFrom(Byte.class)){
            return obj.getByteValue(name);
        }
        if(clazz==short.class || clazz.isAssignableFrom(Short.class)){
            return obj.getShortValue(name);
        }
        if(clazz==long.class || clazz.isAssignableFrom(Long.class)){
            return obj.getLongValue(name);
        }
        if(clazz==float.class || clazz.isAssignableFrom(Float.class)){
            return obj.getFloatValue(name);
        }
        if(clazz==double.class || clazz.isAssignableFrom(Double.class)){
            return obj.getDoubleValue(name);
        }
        return obj.get(name);
    }

    public static boolean readAll(byte[] data, int length, InputStream ins) {
        int pos = 0;
        try {
            do {
                int len = ins.read(data, pos, length);
                if (len == -1) {
                    break;
                }
                pos += len;
                length -= len;
            } while (length > 0);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return (length==0);
    }

    /**
     * 将数组carr从offset开始的length字节转换成16进制文本串
     * @param carr 数据数组
     * @param offset 开始偏移
     * @param length 数据长度
     * @return 16进制文本串
     */
    public static String byteArrayToHexString(byte[] carr, int offset, int length){
        StringBuilder str = new StringBuilder(128);
        for(int i=offset, j=0; i<carr.length && j<length; i++, j++){
            Integer temp = carr[i] & 0xFF;
            if(temp.intValue()<16) {
                str.append("0" + Integer.toString(temp, 16));
            } else {
                str.append(Integer.toString(temp, 16));
            }
        }
        return str.toString();
    }

    /**
     * 二进制串转换字节数组
     * @param text 二进制串
     * @return 字节数组
     */
    public static byte[] hexStringToByteArray(String text){
        byte[] data = new byte[text.length()/2];
        for(int i=0; i<data.length; i++){
            String str = text.substring(2*i, 2*i+2);
            data[i] = (byte)Integer.parseInt(str, 16);
        }
        return data;
    }

    public static String getToken(String cookie){
        if(cookie==null || cookie.length()<6){
            return null;
        }

        int pos1 = cookie.indexOf("token=");
        if(pos1<0){
            return null;
        }
        pos1 += 6;

        String token;
        int pos2 = cookie.indexOf(";");
        if(pos2<0){
            token = cookie.substring(pos1);
        } else {
            token = cookie.substring(pos1, pos2);
        }
        return token;
    }

    /**
     * 获取Multipart提交的数据
     * @param ins 数据流
     * @param type 数据类型
     * @return 表单值
     */
    public static Object getMutltiPart(InputStream ins, String type){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] data = new byte[8192];
            do{
                int len = ins.read(data);
                if(len==-1){
                    break;
                }
                baos.write(data, 0, len);
            }while(true);
            data = baos.toByteArray();
            baos = null;

            type = type.toLowerCase();
            if(type.indexOf("octet-stream")>0){
                return data;
            }

            String charset = "utf-8";
            int pos = type.indexOf("charset=");
            if(pos>0){
                pos += "charset=".length();
                int pos2 = type.indexOf(";", pos);
                if(pos2>0){
                    charset = type.substring(pos, pos2);
                } else {
                    charset = type.substring(pos);
                }
            }
            return new String(data, charset);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
