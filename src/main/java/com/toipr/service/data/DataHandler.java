package com.toipr.service.data;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 数据处理器，每个处理器只负责一个功能
 * 执行压缩/解压缩，加密/解密等操作
 */
public interface DataHandler {
    /**
     * 获取当前数据处理器
     * @return 数据处理器对象
     */
    DataHandler getHandler();

    /**
     * 设置数据处理器，责任链模式
     * 实现类先调用自己的处理逻辑，再调用责任链后一个处理逻辑, 按index顺序从小到大排序
     * @param handler 数据处理器
     * @return 返回排在第一顺位的数据处理器
     */
    DataHandler addHandler(DataHandler handler);

    /**
     * 获取处理顺序
     * @return
     */
    int getIndex();

    /**
     * 设置处理顺序
     * @param index
     */
    void setIndex(int index);

    /**
     * 设置数据处理责任标志
     * @param flags 处理标志
     */
    void setFlags(int flags);

    /**
     * 数据处理责任标志是否存在
     * @param flags 处理标志
     * @return true=已存在
     */
    boolean exists(int flags);

    /**
     * 处理输入流读取的数据
     * @param ins 数据输入流
     * @param flags 处理标志
     * @return 处理后的数据
     */
    byte[] process(InputStream ins, int flags);

    /**
     * 处理数组从off开始、len字节长的数据
     * @param data 数据数组
     * @param off 开始位置
     * @param len 数据长度
     * @param flags 处理标志
     * @return 处理后的数据
     */
    byte[] process(byte[] data, int off, int len, int flags);
}
