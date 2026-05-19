package com.blogsystem.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogsystem.content.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
