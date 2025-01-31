package com.ll.springjwt20250130.post.comment.controller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.domain.post.comment.entity.PostComment;
import com.ll.springjwt20250130.domain.post.post.entity.Post;
import com.ll.springjwt20250130.domain.post.post.service.PostService;
import com.ll.springjwt20250130.domain.post.comment.controller.ApiV1PostCommentController;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentControllerTest {
    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("댓글 다건 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/1/comments")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk());

        List<PostComment> comments = postService
                .findById(1).get().getComments();

        for (int i = 0; i < comments.size(); i++) {
            PostComment postComment = comments.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(postComment.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(postComment.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(postComment.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$[%d].authorId".formatted(i)).value(postComment.getAuthor().getId()))
                    .andExpect(jsonPath("$[%d].authorName".formatted(i)).value(postComment.getAuthor().getName()))
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(postComment.getContent()));
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    void t2() throws Exception {
        Member actor = memberService.findByUsername("user2").get();

        String memberAccessToken = memberService.genAccessToken(actor);

        Post post = postService.findById(1).get();

        PostComment postComment = post.getCommentById(1).get();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1/comments/1")
                                .header("Authorization", "Bearer " + memberAccessToken)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 삭제되었습니다.".formatted(postComment.getId())));

        assertThat(post.getCommentById(1).isEmpty());
    }

    @Test
    @DisplayName("댓글 수정")
    void t3() throws Exception {
        Member actor = memberService.findByUsername("user2").get();
        String memberAccessToken = memberService.genAccessToken(actor);

        Post post = postService.findById(1).get();

        PostComment postComment = post.getCommentById(1).get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1/comments/1")
                                .header("Authorization", "Bearer " + memberAccessToken)
                                .content("""
                                        {
                                            "content": "modify 댓글"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 수정되었습니다.".formatted(postComment.getId())))
                .andExpect(jsonPath("$.data.id").value(postComment.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(postComment.getCreateDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(postComment.getModifyDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.authorId").value(postComment.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(postComment.getAuthor().getName()))
                .andExpect(jsonPath("$.data.content").value("modify 댓글"));
    }

    @Test
    @DisplayName("댓글 작성")
    void t4() throws Exception {
        Member actor = memberService.findByUsername("user2").get();

        String memberAccessToken = memberService.genAccessToken(actor);

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/1/comments")
                                .header("Authorization", "Bearer " + memberAccessToken)
                                .content("""
                                        {
                                            "content": "new write 댓글"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Post post = postService.findById(1).get();
        PostComment lastPostComment = post.getComments().getLast();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("댓글이 작성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(lastPostComment.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(lastPostComment.getCreateDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(lastPostComment.getModifyDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.authorId").value(lastPostComment.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(lastPostComment.getAuthor().getName()))
                .andExpect(jsonPath("$.data.content").value("new write 댓글"));
    }
}
