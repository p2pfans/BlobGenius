package com.toipr.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class HashHelper {
    protected static final int max_idstr_length = 12;
    protected static char[] mychars = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'
    };

    /**
     * 根据哈希文本串生成短码, 长度范围：6-12个字符
     * @param hashBytes 哈希数组
     * @param length 短码长度，默认6个字符
     * @return 短码
     */
    public static String getShortHashStr(byte[] hashBytes, int length){
        if(length<6 || length>max_idstr_length){
            length = 6;
        }

        int step = hashBytes.length / length;
        StringBuilder idstr = new StringBuilder(32);
        for(int i=0, j=0; i<length; i++, j+=step){
            int temp = hashBytes[j];
            if(temp<0){
                temp = -temp;
            }
            temp = temp % mychars.length;
            idstr.append(mychars[temp]);
        }
        return idstr.toString();
    }

    /**
     * 根据哈希文本串生成短码, 长度范围：6-12个字符
     * @param hashStr 数字对象哈希串
     * @param length 短码长度，默认6个字符
     * @return 短码
     */
    public static String getShortHashStr(String hashStr, int length){
        if(length<6 || length>max_idstr_length){
            length = 6;
        }

        int step = hashStr.length() / length;
        StringBuilder idstr = new StringBuilder(32);
        for(int i=0, j=0; i<length; i++, j+=step){
            String str = hashStr.substring(j, j+2);
            int temp = Integer.parseInt(str, 16);
            temp = temp % mychars.length;
            idstr.append(mychars[temp]);
        }
        return idstr.toString();
    }

    /**
     * 创建HASH算法对象
     * @param algHash 算法名称
     * @return 算法实现对象
     */
    public static Object getHashObj(String algHash){
        MessageDigest md = null;
        try{
            md = MessageDigest.getInstance(algHash);
        } catch(Exception ex){
            ;
        }
        return md;
    }

    /**
     * 更新数据内容
     * @param hashObj 哈希算法对象
     * @param data 数据数组
     * @param off 数据偏移
     * @param length 数据长度
     */
    public static void update(Object hashObj, byte[] data, int off, int length){
        MessageDigest md = (MessageDigest)hashObj;
        md.update(data, off, length);
    }

    /**
     * 计算最终哈希码
     * @param hashObj 哈希算法对象
     * @return 哈希码文本串
     */
    public static String finalHash(Object hashObj){
        MessageDigest md = (MessageDigest)hashObj;
        byte[] harr = md.digest();
        return Utils.byteArrayToHexString(harr, 0, harr.length);
    }

    /**
     * 计算最终哈希码
     * @param hashObj 哈希算法对象
     * @return 哈希码二进制数组
     */
    public static byte[] findHashBytes(Object hashObj){
        MessageDigest md = (MessageDigest)hashObj;
        return md.digest();
    }

    /**
     * 计算输入流数据的哈希码
     * @param ins 输入流对象
     * @param algHash 哈希算法名称
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    public static String computeHash(InputStream ins, String algHash) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);

        byte[] buff = new byte[64*1024];
        int length = ins.read(buff);
        while(length>0){
            md.update(buff, 0, length);
            length = ins.read(buff);
        }

        byte[] harr = md.digest();
        return Utils.byteArrayToHexString(harr, 0, harr.length);
    }

    /**
     * 计算输入流数据的哈希码
     * @param ins 输入流对象
     * @param algHash 哈希算法名称
     * @return 哈希码数组
     * @throws Exception
     */
    public static byte[] computeHashBytes(InputStream ins, String algHash) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);

        byte[] buff = new byte[64*1024];
        int length = ins.read(buff);
        while(length>0){
            md.update(buff, 0, length);
            length = ins.read(buff);
        }
        return md.digest();
    }

    public static String computeHash(byte[] data, String algHash) throws Exception {
        return computeHash(data, 0, data.length, algHash);
    }

    public static byte[] computeHashBytes(byte[] data, String algHash) throws Exception {
        return computeHashBytes(data, 0, data.length, algHash);
    }

    /**
     * 计算数组从offset开始length字节数据的哈希码
     * @param data 数据数组
     * @param offset 开始偏移
     * @param length 数据长度
     * @param algHash 哈希算法名称
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    public static String computeHash(byte[] data, int offset, int length, String algHash) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);
        md.update(data, offset, length);

        byte[] harr = md.digest();
        return Utils.byteArrayToHexString(harr, 0, harr.length);
    }

    /**
     * 计算数组从offset开始length字节数据的哈希码
     * @param data 数据数组
     * @param offset 开始偏移
     * @param length 数据长度
     * @param algHash 哈希算法名称
     * @return 哈希码二进制数组
     * @throws Exception
     */
    public static byte[] computeHashBytes(byte[] data, int offset, int length, String algHash) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);
        md.update(data, offset, length);
        return md.digest();
    }

    /**
     * 计算文件哈希校验的16进制文本串
     * @param filePath 文件路径
     * @param algHash 哈希算法
     * @return 哈希码二进制数组
     * @throws Exception
     */
    public static String computeHash(String filePath, String algHash) throws Exception {
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHash(fis, algHash);
        }
    }

    /**
     * 计算文件哈希校验的二进制数组
     * @param filePath 文件路径
     * @param algHash 哈希算法
     * @return 哈希码二进制数组
     * @throws Exception
     */
    public static byte[] computeHashBytes(String filePath, String algHash) throws Exception {
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHashBytes(fis, algHash);
        }
    }

    /**
     * 计算输入流HASH与分块HASH列表
     * @param ins 输入流
     * @param blockSize 文件块大小 默认1MB=1024*1024
     * @param algHash hash算法名称，默认MD5
     * @param hashList 文件块哈希结果列表
     * @return 文件哈希串
     * @throws Exception
     */
    public static String computeHashByBlock(InputStream ins, int blockSize, String algHash, List<String> hashList) throws Exception {
        if(algHash==null || algHash.length()==0){
            algHash = "md5";
        }
        if(blockSize<=0){
            blockSize = 1024*1024;
        }

        MessageDigest md = MessageDigest.getInstance(algHash);

        int pos = 0, length = 0;
        byte[] buff = new byte[blockSize];
        while(true){
            while(pos<blockSize){
                length = ins.read(buff, pos, blockSize - pos);
                if(length==-1){//返回-1表示输入流结束
                    break;
                }
                pos += length;
            }

            if(pos>0) {
                md.update(buff, 0, pos);
                String hash = computeHash(buff, 0, pos, algHash);
                hashList.add(hash);
                if(pos<blockSize){
                    break;
                }
                pos = 0;
            }
        }

        byte[] harr = md.digest();
        return Utils.byteArrayToHexString(harr, 0, harr.length);
    }

    /**
     * 计算文件HASH与文件数据块HASH列表
     * @param filePath 文件路径
     * @param blockSize 文件块大小 默认1MB=1024*1024
     * @param algHash hash算法名称，默认MD5
     * @param hashList 文件块哈希结果列表
     * @return 文件哈希串
     * @throws Exception
     */
    public static String computeHashByBlock(String filePath, int blockSize, String algHash, List<String> hashList) throws Exception {
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHashByBlock(fis, blockSize, algHash, hashList);
        }
    }
}
