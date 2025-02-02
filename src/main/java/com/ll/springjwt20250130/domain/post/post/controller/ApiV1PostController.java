package com.ll.springjwt20250130.domain.post.post.controller;

import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.post.post.dto.PostDto;
import com.ll.springjwt20250130.domain.post.post.dto.PostWithContentDto;
import com.ll.springjwt20250130.domain.post.post.entity.Post;
import com.ll.springjwt20250130.domain.post.post.service.PostService;
import com.ll.springjwt20250130.global.exceptions.ServiceException;
import com.ll.springjwt20250130.global.rq.Rq;
import com.ll.springjwt20250130.global.rsData.RsData;
import com.ll.springjwt20250130.global.standard.page.dto.PageDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;
    private final Rq rq;

    record PostStatisticResBody (
        long totalPostCount,
        long totalPublishedPostCount,
        long totalListedPostCount
    ) {
    }

    @GetMapping("/statistics")
    @Transactional(readOnly = true)
    public PostStatisticResBody statistic() {
        Member actor = rq.getActor();

        return new PostStatisticResBody(
            10,
            10,
            10);
    }

    // 내글 다건 조회
    @GetMapping("/mine")
    @Transactional(readOnly = true)
    public PageDto<PostDto> mine(
            @RequestParam(defaultValue = "title") String searchKeywordType,
            @RequestParam(defaultValue = "") String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Member actor = rq.getActor();

        return new PageDto<>(
                postService.findByAuthorPaged(actor, searchKeywordType, searchKeyword, page, pageSize) // Page<Post>
                        .map(PostDto::new)
        );
    }


    // 다건 조회
    @GetMapping
    @Transactional(readOnly = true)
    public PageDto<PostDto> items(
            @RequestParam(defaultValue = "title") String searchKeywordType,
            @RequestParam(defaultValue = "") String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
       return new PageDto<>(
               postService.findByListedPaged(true, searchKeywordType, searchKeyword, page, pageSize) // Page<Post>
                       .map(PostDto::new) // 외워야함: Page<PostDto>
       );
    }

    // 단건 조회
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostWithContentDto item(@PathVariable long id) {
        Post post = postService.findById(id).get();

        if (!post.isPublished()) {
            Member actor = rq.getActor();

            if (actor == null) {
                throw new ServiceException("401-1", "로그인이 필요합니다.");
            }

            post.checkActorCanRead(actor);
        }

        return new PostWithContentDto(post);
    }

    record postWriteReqBody (
            @NotBlank @Length(min = 2) String title,
            @NotBlank @Length(min = 2) String content,
            boolean published,
            boolean listed
    ) {
    }

    @PostMapping("/write")
    @Transactional
    public RsData<PostWithContentDto> write(
            @RequestBody @Valid postWriteReqBody reqBody
    ) {
        Member actor = rq.getActor();

        Post post = postService.write(
                actor,
                reqBody.title,
                reqBody.content,
                reqBody.published,
                reqBody.listed);

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다".formatted(post.getId()),
                new PostWithContentDto(post));
    }

    record PostModifyReqBody (
            @NotBlank String title,
            @NotBlank String content,
            boolean published,
            boolean listed
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostWithContentDto> modifyItem(
            @PathVariable long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Member actor = rq.getActor();

        Post post = postService.findById(id).get();

        post.checkActorCanModify(actor);

        postService.modify(post, reqBody.title, reqBody.content, reqBody.published, reqBody.listed);

        postService.flush();

        return new RsData<>(
                "200-1",
                "%d번 글 수정이 완료되었습니다".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> deleteItem(
            @PathVariable long id
    ) {
        Member actor = rq.getActor();

        Post post = postService.findById(id).get();

        post.checkActorCanDelete(actor);

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(post.getId())
        );
    }
}
