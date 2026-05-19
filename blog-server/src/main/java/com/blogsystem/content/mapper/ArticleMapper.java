package com.blogsystem.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blogsystem.content.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
