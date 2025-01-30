package com.ll.springjwt20250130.domain.post.comment.controller;

import java.util.List;

import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.post.comment.dto.PostCommentDto;
import com.ll.springjwt20250130.domain.post.comment.entity.PostComment;
import com.ll.springjwt20250130.domain.post.post.entity.Post;
import com.ll.springjwt20250130.domain.post.post.service.PostService;
import com.ll.springjwt20250130.global.exceptions.ServiceException;
import com.ll.springjwt20250130.global.rq.Rq;
import com.ll.springjwt20250130.global.rsData.RsData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;
    private final Rq rq;

    @GetMapping
    @Transactional(readOnly = true)
    public List<PostCommentDto> items(
            @PathVariable long postId
    ) {
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%번 글은 존재하지 않습니다".formatted(postId))
        );

        return post
                .getComments()
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    // 특정 게시글의 특정 댓글을 삭제하는 DELETE 메서드
    @DeleteMapping("/{id}") // 기본 URL에 /id 를 추가
    @Transactional
    public RsData<Void> delete( // 인자는 게시물 id와 댓글 id를 URL에서 받는다
            @PathVariable long postId,
            @PathVariable long id
    ) {
        // 해당 사용자가 권한이 있는지를 확인한다
        Member actor = rq.getActor();

        // 해당 게시물이 존재하는지 확인하고, 만약 없다면 존재하지 않는다고 에러를 처리
        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다".formatted(postId))
        );

        // 해당 게시물이 확인되었으면, 딸려있는 댓글중 삭제하려는 댓글을 확인하고, 없다면 에러를 처리
        PostComment postComment = post.getCommentById(id).orElseThrow(
                () -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다".formatted(id))
        );

        // 해당 객체를 삭제할 권한이 있는지 확인
        postComment.checkActorCanDelete(actor);

        // 권한이 확인되었다면 삭제
        post.removeComment(postComment);

        // 삭제한 후 아래 리턴값을 반환 Void 형 이기 때문에 별도 null을 표기하지 않아도 됨.
        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(id)
        );
    }

    record PostCommentModifyReqBody(
            @NotBlank @Length(min = 2, max = 1000) String content
    ) {
    }

    // 특정 게시글의 특정 댓글을 수정하는 PUT 메서드
    @PutMapping("/{id}") // 기본 URL에 /id 를 추가
    @Transactional
    public RsData<PostCommentDto> modify(
             @PathVariable long postId,
             @PathVariable long id,
             @RequestBody @Valid PostCommentModifyReqBody reqBody
    ) {
        Member actor = rq.getActor();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다".formatted(postId))
        );

        PostComment postComment = post.getCommentById(id).orElseThrow(
                () -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다".formatted(id))
        );

        postComment.checkActorCanModify(actor);

        postComment.modify(reqBody.content);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 수정되었습니다.".formatted(id),
                new PostCommentDto(postComment)
        );
    }

    record PostCommentWriteReqBody(
            @NotBlank @Length(min = 2, max = 1000) String content
    ) {
    }

    // 댓글 작성 매서드
    @PostMapping
    @Transactional
    public RsData<PostCommentDto> write(
            @PathVariable long postId,
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        Member actor = rq.getActor();

        Post post = postService.findById(postId).orElseThrow(
                () -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다".formatted(postId))
        );

        PostComment postComment = post.addComment(actor, reqBody.content);

        postService.flush();

        return new RsData<>(
                "200-1",
                "댓글이 작성되었습니다.",
                new PostCommentDto(postComment)
        );
    }
}
