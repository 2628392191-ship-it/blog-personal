package com.blogsystem.content.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.content.dto.ArticleSaveRequest;
import com.blogsystem.content.dto.CategorySaveRequest;
import com.blogsystem.content.dto.TagSaveRequest;
import com.blogsystem.content.entity.Article;
import com.blogsystem.content.entity.ArticleTag;
import com.blogsystem.content.entity.Category;
import com.blogsystem.content.entity.Tag;
import com.blogsystem.content.mapper.ArticleMapper;
import com.blogsystem.content.mapper.ArticleTagMapper;
import com.blogsystem.content.mapper.CategoryMapper;
import com.blogsystem.content.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    /**
     * 添加一篇文章
     * @param request
     * @return
     */
    @Transactional
    public Long saveArticle(ArticleSaveRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Article article;
        if (request.id() == null) {
            article = new Article();
            article.setAuthorId(userId);
            article.setViewCount(0);
            article.setLikeCount(0);
            article.setCollectCount(0);
            article.setDeleted(0);
        } else {
            article = articleMapper.selectById(request.id());
            if (article == null || article.getDeleted() == 1) {
                throw new IllegalArgumentException("文章不存在");
            }
        }
        article.setTitle(request.title());
        article.setSummary(request.summary());
        article.setContentMd(request.contentMd());
        article.setCoverUrl(request.coverUrl());
        article.setCategoryId(request.categoryId());
        article.setStatus(request.status());
        article.setIsTop(request.isTop() == null ? 0 : request.isTop());
        article.setIsCommentEnabled(request.isCommentEnabled() == null ? 1 : request.isCommentEnabled());
        if (request.status() == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }

        if (request.id() == null) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
        }

        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            for (Long tagId : request.tagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
        return article.getId();
    }

    /**
     * 文章分页查询
     * @param status
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<Article> listArticles(Integer status, Long categoryId, Long tagId, long pageNum, long pageSize) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>().eq(Article::getDeleted, 0)
                .orderByDesc(Article::getIsTop).orderByDesc(Article::getPublishTime).orderByDesc(Article::getId);
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (tagId != null) {
            wrapper.inSql(Article::getId, "select article_id from article_tag where tag_id = " + tagId);
        }
        return articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 热门文章展示
     * @param limit
     * @return
     */
    public List<Article> listHotArticles(long limit) {
        return articleMapper.selectList(new LambdaQueryWrapper<Article>().eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getViewCount)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId)
                .last("limit " + limit));
    }

    /**
     * 拿到一篇文章
     * @param id
     * @return
     */
    public Article getArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new IllegalArgumentException("文章不存在");
        }
        article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
        articleMapper.updateById(article);
        return article;
    }

    /**
     * 删除一篇文章
     * @param id
     */
    public void deleteArticle(Long id) {
        Article article = getArticle(id);
        article.setDeleted(1);
        articleMapper.updateById(article);
    }

    /**
     * 保存分类
     * @param request
     * @return
     */
    public Long saveCategory(CategorySaveRequest request) {
        Category category = request.id() == null ? new Category() : categoryMapper.selectById(request.id());
        if (request.id() != null && category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setSort(request.sort() == null ? 0 : request.sort());
        category.setStatus(request.status() == null ? 1 : request.status());
        category.setDeleted(0);
        if (request.id() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        return category.getId();
    }

    /**
     * 分类展示
     * @return
     */
    public List<Category> listCategory() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSort).orderByDesc(Category::getId));
    }

    /**
     * 删除分类
     * @param id
     */
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new IllegalArgumentException("分类不存在");
        }
        category.setDeleted(1);
        categoryMapper.updateById(category);
    }

    /**
     * 保存一个标签
     * @param request
     * @return
     */
    public Long saveTag(TagSaveRequest request) {
        Tag tag = request.id() == null ? new Tag() : tagMapper.selectById(request.id());
        if (request.id() != null && tag == null) {
            throw new IllegalArgumentException("标签不存在");
        }
        tag.setName(request.name());
        tag.setSlug(request.slug());
        tag.setColor(request.color());
        tag.setSort(request.sort() == null ? 0 : request.sort());
        tag.setStatus(request.status() == null ? 1 : request.status());
        tag.setDeleted(0);
        if (request.id() == null) {
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }
        return tag.getId();
    }

    /**
     * 标签展示
     * @return
     */
    public List<Tag> listTag() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().eq(Tag::getDeleted, 0)
                .orderByAsc(Tag::getSort).orderByDesc(Tag::getId));
    }

    /**
     * 删除标签
     * @param id
     */
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new IllegalArgumentException("标签不存在");
        }
        tag.setDeleted(1);
        tagMapper.updateById(tag);
    }
}
