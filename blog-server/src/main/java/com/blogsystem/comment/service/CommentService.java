package com.blogsystem.comment.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blogsystem.auth.entity.SysUser;
import com.blogsystem.auth.mapper.SysUserMapper;
import com.blogsystem.comment.dto.CommentSaveRequest;
import com.blogsystem.comment.dto.CommentVO;
import com.blogsystem.comment.entity.Comment;
import com.blogsystem.comment.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论业务 —— 发表 / 文章评论列表 / 管理端审核
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 发表评论
     */
    public Long save(CommentSaveRequest request, String ip, String userAgent) {
        Comment comment = new Comment();
        comment.setArticleId(request.articleId());
        comment.setUserId(StpUtil.getLoginIdAsLong());
        comment.setParentId(request.parentId() == null ? 0L : request.parentId());
        comment.setReplyToUserId(request.replyToUserId());
        comment.setContent(request.content());
        comment.setStatus(1);
        comment.setIp(ip);
        comment.setUserAgent(userAgent);
        comment.setDeleted(0);
        commentMapper.insert(comment);
        return comment.getId();
    }

    /**
     * 查询文章可见评论列表，附带用户昵称
     */
    public List<CommentVO> listByArticle(Long articleId) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getDeleted, 0)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getId));

        Set<Long> userIds = new HashSet<>();
        for (Comment c : comments) {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null && c.getReplyToUserId() > 0) {
                userIds.add(c.getReplyToUserId());
            }
        }
        Map<Long, String> nicknames = userIds.isEmpty() ? Map.of() :
                sysUserMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u.getNickname() != null ? u.getNickname() : u.getUsername()));

        return comments.stream().map(c -> new CommentVO(
                c.getId(), c.getArticleId(), c.getUserId(),
                nicknames.getOrDefault(c.getUserId(), "用户"),
                c.getParentId(), c.getReplyToUserId(),
                c.getReplyToUserId() != null ? nicknames.getOrDefault(c.getReplyToUserId(), "用户") : null,
                c.getContent(), c.getCreatedAt()
        )).collect(Collectors.toList());
    }

    /**
     * 管理端评论分页查询，支持按状态筛选
     */
    public Page<Comment> adminList(Integer status, long pageNum, long pageSize) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDeleted, 0)
                .orderByDesc(Comment::getId);
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        return commentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 管理端审核评论（通过/隐藏）
     */
    public void adminAudit(Long id, Integer status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new IllegalArgumentException("评论不存在");
        }
        comment.setStatus(status);
        commentMapper.updateById(comment);
    }

    /**
     * 管理端删除评论（软删除）
     */
    public void adminDelete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        commentMapper.deleteById(id);
    }
}
