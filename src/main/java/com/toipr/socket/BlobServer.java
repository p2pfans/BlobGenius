package com.toipr.socket;

public interface BlobServer {
    /**
     * 初始化服务器
     * @param args 自定义参数
     * @return true=成功
     */
    boolean init(Object... args);

    /**
     * 关闭服务器
     */
    void close();

    /**
     * 添加存储的资源与本地路径
     * @param rid 资源ID
     * @param filePath 文件路径
     */
    void addSource(String rid, String filePath);
}
