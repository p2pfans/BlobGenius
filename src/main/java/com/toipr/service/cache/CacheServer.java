package com.toipr.service.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CacheServer {
    int ifNotExists = 1;
    int onlyExists = 2;

    /**
     * 数据节点类型
     */
    int prevNode = -1;
    int nextNode = 1;
    int primary = 2;

    /**
     * 责任链传导方向 direction
     */
    int noChain = 0;       //禁止传导
    int prevChain = 1;     //前向传导
    int nextChain = 2;     //后向传导
    int biChain = 3;       //双向传导
    int priChain = 4;      //主节点传导
    int allChain = 7;      //禁止传导

    /**
     * 设置下一个处理的数据服务器，责任链设计模式
     * 解决数据多节点复制 与 失败多点重试问题
     * @param type -1=前一个 1=后一个 0=主服务器
     * @param server 数据服务器
     */
    void setServer(int type, CacheServer server);

    /**
     * 向频道channel发布消息message
     * @param channel 频道名称
     * @param message 消息内容
     * @return 成功=true 失败=false
     */
    boolean publish(String channel, String message);

    /**
     * 设置缓存失效时间
     * @param key 缓存名称
     * @param seconds 失效时间 单位秒s
     */
    void expire(String key, int seconds);

    /**
     * 判断缓存是否存在
     * @param key 缓存名称
     * @return true=存在
     */
    boolean exists(String key);
    long exists(String... keys);

    /**
     * 添加缓存对象
     * @param key 缓存名称
     * @param expire 失效时间 单位ms
     * @param flags 缓存属性
     * @param value 缓存值
     * @return 成功=true 失败=false
     */
    boolean addCache(String key, int expire, int flags, Object value);
    /**
     * 添加缓存对象，从target对象获取name属性
     * @param key 缓存名称
     * @param expire 失效时间 单位ms
     * @param flags 缓存属性
     * @param name 对象属性
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    boolean addCache(String key, int expire, int flags, String name, Object target);

    /**
     * 添加BEAN对象
     * @param key 缓存名称
     * @param expire 失效时间 单位ms
     * @param flags 缓存属性
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    boolean addObject(String key, int expire, int flags, Object target);

    /**
     * 添加哈希缓存
     * @param map 哈希名称
     * @param flags 缓存属性
     * @param field 缓存字段名称
     * @param value 对象实例
     * @return 成功=true 失败=false
     */
    boolean addMapCache(String map, int flags, String field, Object value);

    /**
     * 添加哈希缓存，从target对象获取name属性
     * @param map 哈希名称
     * @param flags 缓存属性
     * @param field 缓存字段名称
     * @param name 对象属性名称
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    boolean addMapCache(String map, int flags, String field, String name, Object target);

    /**
     * 添加集合缓存
     * @param set 集合名称
     * @param score 排序值，-1=无序集合
     * @param value 属性值
     * @return 成功=true 失败=false
     */
    boolean addSetCache(String set, double score, Object value);

    /**
     * 添加集合缓存，从target对象获取name属性
     * @param set 集合名称
     * @param score 排序值，-1=无序集合
     * @param name 属性值
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    boolean addSetCache(String set, double score, String name, Object target);

    /**
     * 添加列表缓存
     * @param list 列表名称
     * @param isHead true=添加到头部 false=添加到尾部
     * @param value 属性值
     * @return 成功=true 失败=false
     */
    boolean addListCache(String list, boolean isHead, Object value);

    /**
     * 添加列表缓存
     * @param list 缓存名称
     * @param isHead true=添加到头部 false=添加到尾部
     * @param name 属性名称
     * @param target 对象实例
     * @return 成功=true 失败=false
     */
    boolean addListCache(String list, boolean isHead, String name, Object target);

    /**
     * 删除缓存
     * @param name 缓存名称
     */
    void removeCache(String name);

    /**
     * 删除哈希缓存
     * @param name 缓存名称
     * @param key 哈希主键
     */
    void removeCache(String name, String key);

    /**
     * 获取字符串缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    String getString(String name, String defValue);
    String getString(String name, String key, String defValue);

    /**
     * 获取布尔缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    boolean getBoolean(String name, boolean defValue);
    boolean getBoolean(String name, String key, boolean defValue);

    /**
     * 获取整数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    int getInteger(String name, int defValue);
    int getInteger(String name, String key, int defValue);

    /**
     * 获取大整数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    long getLong(String name, long defValue);
    long getLong(String name, String key, long defValue);

    /**
     * 获取浮点数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    float getFloat(String name, float defValue);
    float getFloat(String name, String key, float defValue);

    /**
     * 获取双精度浮点数缓存
     * @param name 缓存名称
     * @param defValue 缺省值
     * @return 缓存值
     */
    double getDouble(String name, double defValue);
    double getDouble(String name, String key, double defValue);

    /**
     * 获取JSON对象，要求存储的值符合JSON规范
     * @param name 缓存名称
     * @return JSON对象
     */
    Object getJsonObject(String name);
    Object getJsonObject(String name, String key);

    /**
     * 获取缓存值，并初始化一个BEAN对象
     * @param name 缓存名称
     * @param clazz 对象类型
     * @return 失败=null
     */
    Object getObject(String name, Class<?> clazz);

    /**
     * 获取集合缓存
     * @param name 缓存名称
     * @return
     */
    Set<String> getSet(String name);

    /**
     * 获取列表缓存
     * @param name 缓存名称
     * @return
     */
    List<String> getList(String name);

    /**
     * 获取哈希缓存
     * @param name 缓存名称
     * @return
     */
    Map<String, String> getMap(String name);
}
