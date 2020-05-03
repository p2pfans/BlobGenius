package com.toipr.model.data;

public class DataConst {
    /**
     * 等待执行
     */
    public static final int State_Waiting = 0;
    /**
     * 准备执行
     */
    public static final int State_Prepare = 1;
    /**
     * 正在执行
     */
    public static final int State_Active = 2;
    /**
     * 终止执行
     */
    public static final int State_Cancel = 3;
    /**
     * 执行错误
     */
    public static final int State_Error = 5;
    /**
     * 完成执行
     */
    public static final int State_Completed = 7;

    /**
     * 默认资源目录
     */
    public static final String defaultDirectory = "root";

    /**
     * 默认资源ID
     */
    public static final String defaultResource = "Ky4bJyO3";

    /**
     * 数据块默认尺寸1024KB，针对大文件切分管用
     */
    public static final int DataBlob_DefSize = 1024*1024;

    /**
     * 小数字对象数据块
     */
    public static final int BlobSize_256B = 256;
    public static final int BlobSize_1KB = 1024;
    public static final int BlobSize_2KB = 2048;
    public static final int BlobSize_4KB = 4*1024;

    /**
     * 大数字对象数据块
     */
    public static final int BlobSize_256KB = 256*1024;
    public static final int BlobSize_512KB = 512*1024;
    public static final int BlobSize_1024KB = 1024*1024;
    public static final int BlobSize_2048KB = 2*1024*1024;

    /**
     * 数据编码后存储
     */
    public static final int DataFlags_Encode = 1;
    /**
     * 数据已加密
     */
    public static final int DataFlags_Cipher = 2;
    /**
     * 数据已压缩
     */
    public static final int DataFlags_Compress = 4;
    /**
     * 目录对象
     */
    public static final int DataFlags_Directory = 8;
    /**
     * 流对象
     */
    public static final int DataFlags_Stream = 16;


    /**
     * 默认失败重试次数
     */
    public static int def_fail_retry = 3;

    /**
     * 默认最多500个文件或目录入等待队列
     */
    public static int max_file_inqueue = 100;
    /**
     * 默认每个线程最多3个线程同时上传
     */
    public static int max_thread_a_file = 3;

    /**
     * 底层三种数据类型，数据对象objects, 数据块索引blobIds, 数据块blobs
     */
    public static final String DataType_Object = "objects";
    public static final String DataType_BlobRef = "blobref";
    public static final String DataType_Blob = "blobs";

    /**
     * 正常服务
     */
    public static final int Object_Normal = 0;
    /**
     * 数据不完整
     */
    public static final int Object_Incomplete = 1;
    /**
     * 禁止访问
     */
    public static final int Object_Banned = 9;
    /**
     * 数据错误，待修复
     */
    public static final int Object_Error = 11;
    /**
     * 删除标记，删除程序择时处理
     */
    public static final int Object_Erasable = 13;
}
