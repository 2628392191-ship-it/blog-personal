package com.blogsystem.content.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.content.dto.ArticleSaveRequest;
import com.blogsystem.content.dto.CategorySaveRequest;
import com.blogsystem.content.dto.TagSaveRequest;
import com.blogsystem.content.entity.Article;
import com.blogsystem.content.entity.ArticleLike;
import com.blogsystem.content.entity.ArticleTag;
import com.blogsystem.content.entity.Category;
import com.blogsystem.content.entity.Tag;
import com.blogsystem.content.mapper.ArticleLikeMapper;
import com.blogsystem.content.mapper.ArticleMapper;
import com.blogsystem.content.mapper.ArticleTagMapper;
import com.blogsystem.content.mapper.CategoryMapper;
import com.blogsystem.content.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleLikeMapper articleLikeMapper;
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
        article.setContentHtml(mdToHtml(request.contentMd()));
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
     * 文章分页查询，结果附带分类名称和标签名称
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
        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Article> records = page.getRecords();
        if (records.isEmpty()) {
            return page;
        }

        Set<Long> categoryIds = records.stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c.getName() != null ? c.getName() : ""));

        Set<Long> articleIds = records.stream().map(Article::getId).collect(Collectors.toSet());
        List<ArticleTag> allMappings = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        Map<Long, List<Long>> articleTagIdMap = allMappings.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(ArticleTag::getTagId, Collectors.toList())));

        Set<Long> allTagIds = allMappings.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> tagNameMap = allTagIds.isEmpty() ? Map.of() :
                tagMapper.selectBatchIds(allTagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, t -> t.getName() != null ? t.getName() : ""));

        for (Article a : records) {
            a.setCategoryName(categoryNameMap.get(a.getCategoryId()));
            List<Long> tagIdsForArticle = articleTagIdMap.getOrDefault(a.getId(), List.of());
            a.setTagNames(tagIdsForArticle.stream().map(tagNameMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return page;
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
     * 删除一篇文章（利用 MyBatis-Plus 逻辑删除）
     * @param id
     */
    public void deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        articleMapper.deleteById(id);
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
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        categoryMapper.deleteById(id);
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

    private static final Parser MD_PARSER;
    private static final HtmlRenderer MD_RENDERER;
    static {
        MutableDataSet options = new MutableDataSet();
        MD_PARSER = Parser.builder(options).build();
        MD_RENDERER = HtmlRenderer.builder(options).build();
    }

    private String mdToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        return MD_RENDERER.render(MD_PARSER.parse(markdown));
    }

    /**
     * 切换文章点赞，返回当前是否已点赞
     */
    @Transactional
    public boolean toggleLike(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new IllegalArgumentException("文章不存在");
        }
        ArticleLike exist = articleLikeMapper.selectOne(new LambdaQueryWrapper<ArticleLike>()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)
                .last("limit 1"));
        if (exist != null) {
            articleLikeMapper.deleteById(exist.getId());
            article.setLikeCount(Math.max(0, (article.getLikeCount() == null ? 0 : article.getLikeCount()) - 1));
            articleMapper.updateById(article);
            return false;
        }
        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(userId);
        articleLikeMapper.insert(like);
        article.setLikeCount((article.getLikeCount() == null ? 0 : article.getLikeCount()) + 1);
        articleMapper.updateById(article);
        return true;
    }

    /**
     * 删除标签
     * @param id
     */
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new IllegalArgumentException("标签不存在");
        }
        tagMapper.deleteById(id);
    }
}
