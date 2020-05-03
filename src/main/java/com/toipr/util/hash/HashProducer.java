package com.toipr.util.hash;

import java.io.InputStream;

public interface HashProducer {
    /**
     * 设置哈希消费者
     * @param consumer 消费者接口实例
     */
    void setConsumer(HashConsumer consumer);

    /**
     * 计算输入流数据的哈希码
     * @param ins 输入流对象
     * @param algHash 哈希算法名称
     * @param userData 自定义数据
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    String computeHash(InputStream ins, String algHash, Object userData) throws Exception;

    /**
     * 计算输入流数据的哈希码
     * @param ins 输入流对象
     * @param algHash 哈希算法名称
     * @param userData 自定义数据
     * @return 哈希码数组
     * @throws Exception
     */
    byte[] computeHashBytes(InputStream ins, String algHash, Object userData) throws Exception;

    /**
     * 计算数组data哈希码
     * @param data 数据数组
     * @param algHash 哈希算法
     * @param userData 自定义数据
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    String computeHash(byte[] data, String algHash, Object userData) throws Exception;

    /**
     * 计算数组data哈希码
     * @param data 数据数组
     * @param algHash 哈希算法
     * @param userData 自定义数据
     * @return 哈希码字节数组
     * @throws Exception
     */
    byte[] computeHashBytes(byte[] data, String algHash, Object userData) throws Exception;

    /**
     * 计算数组从offset开始length字节数据的哈希码
     * @param data 数据数组
     * @param offset 开始偏移
     * @param length 数据长度
     * @param algHash 哈希算法名称
     * @return 哈希码16进制文本串
     * @throws Exception
     */
    String computeHash(byte[] data, int offset, int length, String algHash, Object userData) throws Exception;

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
    byte[] computeHashBytes(byte[] data, int offset, int length, String algHash, Object userData) throws Exception;

    /**
     * 计算文件哈希校验的16进制文本串
     * @param filePath 文件路径
     * @param algHash 哈希算法
     * @return 哈希码二进制数组
     * @throws Exception
     */
    String computeHash(String filePath, String algHash, Object userData) throws Exception;

    /**
     * 计算文件哈希校验的二进制数组
     * @param filePath 文件路径
     * @param algHash 哈希算法
     * @return 哈希码二进制数组
     * @throws Exception
     */
    byte[] computeHashBytes(String filePath, String algHash, Object userData) throws Exception;

    /**
     * 计算输入流HASH与分块HASH列表
     * @param ins 输入流
     * @param blockSize 文件块大小 默认1MB=1024*1024
     * @param algHash hash算法名称，默认MD5
     * @return 文件哈希串
     * @throws Exception
     */
    String computeHashByBlock(InputStream ins, int blockSize, String algHash, Object userData) throws Exception;
    byte[] computeHashBytesByBlock(InputStream ins, int blockSize, String algHash, Object userData) throws Exception;


    /**
     * 计算文件HASH与文件数据块HASH列表
     * @param filePath 文件路径
     * @param blockSize 文件块大小 默认1MB=1024*1024
     * @param algHash hash算法名称，默认MD5
     * @return 文件哈希串
     * @return
     **/
    String computeHashByBlock(String filePath, int blockSize, String algHash, Object userData) throws Exception;
    byte[] computeHashBytesByBlock(String filePath, int blockSize, String algHash, Object userData) throws Exception;
}
