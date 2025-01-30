package com.ll.springjwt20250130.domain.post.comment.dto;

import java.time.LocalDateTime;

import com.ll.springjwt20250130.domain.post.comment.entity.PostComment;

import lombok.Getter;

@Getter
public class PostCommentDto {
    private long id;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private long postId;

    private long authorId;

    private String authorName;

    private String content;

    public PostCommentDto(PostComment postcomment) {
        this.id = postcomment.getId();
        this.createDate = postcomment.getCreateDate();
        this.modifyDate = postcomment.getModifyDate();
        this.postId = postcomment.getPost().getId();
        this.authorId = postcomment.getAuthor().getId();
        this.authorName = postcomment.getAuthor().getName();
        this.content = postcomment.getContent();
    }
}
