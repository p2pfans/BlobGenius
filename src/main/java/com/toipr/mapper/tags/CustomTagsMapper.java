package com.toipr.mapper.tags;

import com.toipr.model.tags.CustomTags;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomTagsMapper {
    /**
     * 添加自定义栏目标签
     * @param tags 栏目标签对象
     * @return 返回受影响的行数，成功返回大于0值
     */
    int addCustomTags(@Param("tags") CustomTags tags);

    /**
     * 删除id指定的栏目标签
     * @param id 栏目标签ID
     * @return 返回受影响的行数，成功返回大于0值
     */
    int removeCustomTags(@Param("id") int id);

    /**
     * 根据ID获取栏目标签对象
     * @param id 栏目标签ID
     * @return 栏目标签对象
     */
    CustomTags getCustomTags(@Param("id") int id);

    /**
     * 获取栏目标签列表
     * @param type 标签类型 type=空、获取全部，type=非空、指定类型
     * @return 栏目标签列表
     */
    List<CustomTags> getAllCustomTags(@Param("type") String type);

    /**
     * 根据参数判断栏目标签是否存在
     * @param type 标签类型
     * @param key 标签主键
     * @param pid 父标签ID，可选
     * @return 成功返回栏目标签ID，失败返回null
     */
    Object customTagsExists(@Param("type") String type, @Param("key") String key, @Param("pid") String pid);
}
