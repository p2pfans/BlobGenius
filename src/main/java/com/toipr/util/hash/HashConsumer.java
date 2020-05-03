package com.toipr.util.hash;


public interface HashConsumer {
    /**
     * 通知完成数据块的HASH校验
     * @param data 数据块
     * @param off 数据偏移
     * @param len 数据长度
     * @param hashObj 哈希码，byte[] 或 String
     * @param isByteArr true=哈希码为byte[], false=哈希码为String
     * @param userData 自定义回调数据
     * @return true=继续执行 false=中断执行返回null
     */
    boolean onHashBlock(byte[] data, int off, int len, Object hashObj, boolean isByteArr, Object userData);

    /**
     * 通知哈希码计算完成
     * @param hashObj 哈希码，byte[] 或 String
     * @param isByteArr true=哈希码为byte[], false=哈希码为String
     * @param userData 自定义回调数据
     * @return true=继续执行 false=中断执行返回null
     */
    boolean  onHashComplete(Object hashObj, boolean isByteArr, Object userData);
}
