package com.toipr.util.hash.impl;

import com.toipr.util.HashHelper;
import com.toipr.util.Utils;
import com.toipr.util.hash.HashConsumer;
import com.toipr.util.hash.HashProducer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class DefaultHashProducer implements HashProducer {
    private HashConsumer consumer;

    public DefaultHashProducer(){

    }

    /**
     * 设置哈希消费者
     * @param consumer 消费者接口实例
     */
    public void setConsumer(HashConsumer consumer){
        this.consumer = consumer;
    }

    /**
     * 计算输入流数据的哈希码
     * @param ins 输入流对象
     * @param algHash 哈希算法名称
     * @param userData 自定义数据
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    public String computeHash(InputStream ins, String algHash, Object userData) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);

        byte[] buff = new byte[64*1024];
        int length = ins.read(buff);
        while(length>0){
            md.update(buff, 0, length);
            length = ins.read(buff);
        }

        byte[] harr = md.digest();
        String hash = Utils.byteArrayToHexString(harr, 0, harr.length);
        if(consumer!=null){
            if(!consumer.onHashComplete(hash, false, userData)){
                return null;
            }
        }
        return hash;
    }

    /**
     * 计算输入流数据的哈希码
     * @param ins 输入流对象
     * @param algHash 哈希算法名称
     * @return 哈希码数组
     * @throws Exception
     */
    public byte[] computeHashBytes(InputStream ins, String algHash, Object userData) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);

        byte[] buff = new byte[64*1024];
        int length = ins.read(buff);
        while(length>0){
            md.update(buff, 0, length);
            length = ins.read(buff);
        }

        byte[] harr = md.digest();
        if(consumer!=null){
            if(!consumer.onHashComplete(harr, true, userData)){
                return null;
            }
        }
        return harr;
    }

    /**
     * 计算数组data哈希码
     * @param data 数据数组
     * @param algHash 哈希算法
     * @param userData 自定义数据
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    public String computeHash(byte[] data, String algHash, Object userData) throws Exception {
        return computeHash(data, 0, data.length, algHash, userData);
    }

    /**
     * 计算数组data哈希码
     * @param data 数据数组
     * @param algHash 哈希算法
     * @param userData 自定义数据
     * @return 哈希码字节数组
     * @throws Exception
     */
    public byte[] computeHashBytes(byte[] data, String algHash, Object userData) throws Exception {
        return computeHashBytes(data, 0, data.length, algHash, userData);
    }

    /**
     * 计算数组从offset开始length字节数据的哈希码
     * @param data 数据数组
     * @param offset 开始偏移
     * @param length 数据长度
     * @param algHash 哈希算法名称
     * @param userData 自定义回调数据
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    public String computeHash(byte[] data, int offset, int length, String algHash, Object userData) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);
        md.update(data, offset, length);

        byte[] harr = md.digest();
        String hash = Utils.byteArrayToHexString(harr, 0, harr.length);
        if(consumer!=null){
            if(!consumer.onHashBlock(data, offset, length, hash, false, userData)){
                return null;
            }
        }
        return hash;
    }

    /**
     * 计算数组从offset开始length字节数据的哈希码
     * @param data 数据数组
     * @param offset 开始偏移
     * @param length 数据长度
     * @param algHash 哈希算法名称
     * @param userData 自定义回调数据
     * @return 哈希码二进制数组
     * @throws Exception
     */
    public byte[] computeHashBytes(byte[] data, int offset, int length, String algHash, Object userData) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algHash);
        md.update(data, offset, length);

        byte[] harr = md.digest();
        String hash = Utils.byteArrayToHexString(harr, 0, harr.length);
        if(consumer!=null){
            if(!consumer.onHashBlock(data, offset, length, harr, true, userData)){
                return null;
            }
        }
        return harr;
    }

    /**
     * 计算文件哈希校验的16进制文本串
     * @param filePath 文件路径
     * @param algHash 哈希算法
     * @param userData 自定义回调数据
     * @return 哈希码二进制数组
     * @throws Exception
     */
    public String computeHash(String filePath, String algHash, Object userData) throws Exception {
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHash(fis, algHash, userData);
        }
    }

    /**
     * 计算文件哈希校验的二进制数组
     * @param filePath 文件路径
     * @param algHash 哈希算法
     * @param userData 自定义回调数据
     * @return 哈希码二进制数组
     * @throws Exception
     */
    public byte[] computeHashBytes(String filePath, String algHash, Object userData) throws Exception {
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHashBytes(fis, algHash, userData);
        }
    }

    /**
     * 计算输入流HASH与分块HASH列表
     * @param ins 输入流
     * @param blockSize 文件块大小 默认1MB=1024*1024
     * @param algHash hash算法名称，默认MD5
     * @param userData 自定义回调数据
     * @return 文件哈希串
     * @throws Exception
     */
    public String computeHashByBlock(InputStream ins, int blockSize, String algHash, Object userData) throws Exception {
        if(algHash==null || algHash.length()==0){
            algHash = "SHA-256";
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
                if(computeHash(buff, 0, pos, algHash, userData)==null){
                    break;
                }
                if(pos<blockSize){
                    break;
                }
                pos = 0;
            }
        }

        byte[] harr = md.digest();
        String hash = Utils.byteArrayToHexString(harr, 0, harr.length);
        if(consumer!=null){
            if(!consumer.onHashComplete(hash, false, userData)){
                return null;
            }
        }
        return hash;
    }
    public byte[] computeHashBytesByBlock(InputStream ins, int blockSize, String algHash, Object userData) throws Exception{
        if(algHash==null || algHash.length()==0){
            algHash = "SHA-256";
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
                if(computeHashBytes(buff, 0, pos, algHash, userData)==null){
                    break;
                }
                if(pos<blockSize){
                    break;
                }
                pos = 0;
            }
        }

        byte[] harr = md.digest();
        if(consumer!=null){
            if(!consumer.onHashComplete(harr, true, userData)){
                return null;
            }
        }
        return harr;
    }

    /**
     * 计算文件HASH与文件数据块HASH列表
     * @param filePath 文件路径
     * @param blockSize 文件块大小 默认1MB=1024*1024
     * @param algHash hash算法名称，默认MD5
     * @param userData 自定义回调数据
     * @return 文件哈希串
     * @throws Exception
     */
    public String computeHashByBlock(String filePath, int blockSize, String algHash, Object userData) throws Exception {
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHashByBlock(fis, blockSize, algHash, userData);
        }
    }
    public byte[] computeHashBytesByBlock(String filePath, int blockSize, String algHash, Object userData) throws Exception{
        try(FileInputStream fis = new FileInputStream(filePath)){
            return computeHashBytesByBlock(fis, blockSize, algHash, userData);
        }
    }
}
