package com.ll.springjwt20250130.post.post.contoller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.domain.post.post.controller.ApiV1PostController;
import com.ll.springjwt20250130.domain.post.post.entity.Post;
import com.ll.springjwt20250130.domain.post.post.service.PostService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {
        @Autowired // 테스트는 의존성 주입을 Autowired 를 사용하여 강제로 해야 함
        private MemberService memberService;

        @Autowired
        private PostService postService;

        @Autowired
        private MockMvc mvc;

        @Test
        @DisplayName("1번글 조회")
        void t1() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts/1"))
                                .andDo(print());

                Post post = postService.findById(1).get();

                resultActions
                        .andExpect(handler().handlerType(ApiV1PostController.class))
                        .andExpect(handler().methodName("item"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(post.getId()))
                        .andExpect(jsonPath("$.createDate").value(
                                        Matchers.startsWith(post.getCreateDate().toString().substring(0, 20))))
                        .andExpect(jsonPath("$.modifyDate").value(
                                        Matchers.startsWith(post.getModifyDate().toString().substring(0, 20))))
                        .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                        .andExpect(jsonPath("$.authorName").value(post.getAuthor().getName()))
                        .andExpect(jsonPath("$.title").value(post.getTitle()))
                        .andExpect(jsonPath("$.content").value(post.getContent()))
                        .andExpect(jsonPath("$.published").value(post.isPublished()))
                        .andExpect(jsonPath("$.listed").value(post.isListed()));
        }

        @Test
        @DisplayName("존재하지 않는 글 조회")
        void t2() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts/10"))
                                .andDo(print());

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("item"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.resultCode").value("404-1"))
                                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
        }

        @Test
        @DisplayName("글 작성")
        void t3() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                    .perform(
                        post("/api/v1/posts/write")
                                .header("Authorization", "Bearer " + actorAuthToken)
                                .content("""
                                                {
                                                    "title": "글1",
                                                    "content": "글1의 내용",
                                                    "published": true,
                                                    "listed": false
                                                }
                                                """)
                                .contentType(
                                                new MediaType(MediaType.APPLICATION_JSON,
                                                                StandardCharsets.UTF_8)))
                                .andDo(print());

                Post post = postService.findLaTest().get();

                assertThat(post.getAuthor()).isEqualTo(actor);

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("write"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.resultCode").value("201-1"))
                                .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다".formatted(post.getId())))
                                .andExpect(jsonPath("$.data.id").value(post.getId()))
                                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getName()))
                                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                                .andExpect(jsonPath("$.data.published").value(post.isPublished()))
                                .andExpect(jsonPath("$.data.listed").value(post.isListed()));

        }

        @Test
        @DisplayName("글 작성, with no actor")
        void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/write")
                                .content("""
                                        {
                                            "title": "글1",
                                            "content": "글1의 내용"
                                        }
                                        """)
                                .contentType(
                                    new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.resultCode").value("401-1"))
                                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
        }

        @Test
        @DisplayName("글 수정")
        void t5() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                Post post = postService.findById(1).get();

                LocalDateTime oldModifyDate = post.getModifyDate();

                ResultActions resultActions = mvc
                                .perform(
                                                put("/api/v1/posts/1")
                                                                .header("Authorization", "Bearer " + actorAuthToken)
                                                                .content("""
                                                                                {
                                                                                    "title" : "글1의 수정 제목",
                                                                                    "content": "글1의 수정 내용",
                                                                                    "published": true,
                                                                                    "listed": false
                                                                                }
                                                                                """)
                                                                .contentType(
                                                                                new MediaType(MediaType.APPLICATION_JSON,
                                                                                                StandardCharsets.UTF_8)))
                                .andDo(print());

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("modifyItem"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value("200-1"))
                                .andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다".formatted(post.getId())))
                                .andExpect(jsonPath("$.data.id").value(post.getId()))
                                .andExpect(jsonPath("$.data.createDate").value(
                                                Matchers.startsWith(post.getCreateDate().toString().substring(0, 20))))
                                .andExpect(jsonPath("$.data.modifyDate").value(Matchers
                                                .not(Matchers.startsWith(oldModifyDate.toString().substring(0, 20)))))
                                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getName()))
                                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                                .andExpect(jsonPath("$.data.published").value(true))
                                .andExpect(jsonPath("$.data.listed").value(false));
        }

        @Test
        @DisplayName("글 수정, with no actor")
        void t6() throws Exception {
                ResultActions resultActions = mvc
                    .perform(
                        put("/api/v1/posts/1")
                            .content("""
                                {
                                    "title" : "글1의 수정 제목",
                                    "content": "글1의 수정 내용"
                                }
                                """)
                            .contentType(
                                new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                    .andDo(print());

                resultActions
                        .andExpect(jsonPath("$.resultCode").value("401-1"))
                        .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
        }

        @Test
        @DisplayName("글 수정, with no permission")
        void t7() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                                .perform(
                                                put("/api/v1/posts/3")
                                                                .header("Authorization", "Bearer " + actorAuthToken)
                                                                .content("""
                                                                                {
                                                                                    "title" : "글1의 수정 제목",
                                                                                    "content": "글1의 수정 내용"
                                                                                }
                                                                                """)
                                                                .contentType(
                                                                                new MediaType(MediaType.APPLICATION_JSON,
                                                                                                StandardCharsets.UTF_8)))
                                .andDo(print());

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("modifyItem"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.resultCode").value("403-1"))
                                .andExpect(jsonPath("$.msg").value("작성자만 글을 수정할 수 있습니다."));
        }

        @Test
        @DisplayName("글 삭제")
        @WithUserDetails("user1")
        void t8() throws Exception {
                ResultActions resultActions = mvc
                    .perform(
                        delete("/api/v1/posts/1"))
                    .andDo(print());

                resultActions
                    .andExpect(handler().handlerType(ApiV1PostController.class))
                    .andExpect(handler().methodName("deleteItem"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200-1"))
                    .andExpect(jsonPath("$.msg").value("1번 글이 삭제되었습니다."));

                assertThat(postService.findById(1)).isEmpty();
        }

        @Test
        @DisplayName("글 삭제, with not existing post id")
        void t9() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                                .perform(
                                                delete("/api/v1/posts/10")
                                                                .header("Authorization", "Bearer " + actorAuthToken))
                                .andDo(print());

                assertThat(postService.findById(10)).isEmpty();

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("deleteItem"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.resultCode").value("404-1"))
                                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
        }

        @Test
        @DisplayName("글 삭제, no actor")
        void t10() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                delete("/api/v1/posts/1"))
                                .andDo(print());

                resultActions
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.resultCode").value("401-1"))
                                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
        }

        @Test
        @DisplayName("글 삭제, with no permission")
        void t11() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                                .perform(
                                                delete("/api/v1/posts/3")
                                                                .header("Authorization", "Bearer " + actorAuthToken))
                                .andDo(print());

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("deleteItem"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.resultCode").value("403-1"))
                                .andExpect(jsonPath("$.msg").value("작성자만 글을 삭제할 수 있습니다."));
        }

        @Test
        @DisplayName("비공개 글 6번 조회, with 작성자")
        void t12() throws Exception {
                Member actor = memberService.findByUsername("user4").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts/6")
                                                                .header("Authorization", "Bearer " + actorAuthToken))
                                .andDo(print());

                Post post = postService.findById(6).get();

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("item"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(post.getId()))
                                .andExpect(jsonPath("$.createDate").value(
                                                Matchers.startsWith(post.getCreateDate().toString().substring(0, 25))))
                                .andExpect(jsonPath("$.modifyDate").value(
                                                Matchers.startsWith(post.getModifyDate().toString().substring(0, 25))))
                                .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                                .andExpect(jsonPath("$.authorName").value(post.getAuthor().getName()))
                                .andExpect(jsonPath("$.title").value(post.getTitle()))
                                .andExpect(jsonPath("$.content").value(post.getContent()));
        }

        @Test
        @DisplayName("비공개 글 6번 조회, with no actor")
        void t13() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts/6"))
                                .andDo(print());

                resultActions
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.resultCode").value("401-1"))
                                .andExpect(jsonPath("$.msg").value("로그인이 필요합니다."));
        }

        @Test
        @DisplayName("비공개 글 6번 조회, with no permission")
        void t14() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                    .perform(
                        get("/api/v1/posts/6")
                            .header("Authorization", "Bearer " + actorAuthToken))
                    .andDo(print());

                resultActions
                    .andExpect(handler().handlerType(ApiV1PostController.class))
                    .andExpect(handler().methodName("item"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.resultCode").value("403-1"))
                    .andExpect(jsonPath("$.msg").value("비공개 글은 작성자만 볼 수 있습니다."));
        }

        @Test
        @DisplayName("다건 조회")
        void t15() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts?page=1&pageSize=3"))
                                .andDo(print());

                Page<Post> postPage = postService.findByListedPaged(true, 1, 3);

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("items"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                                .andExpect(jsonPath("$.currentPageNumber").value(postPage.getNumber() + 1))
                                .andExpect(jsonPath("$.pageSize").value(postPage.getSize()));

                List<Post> posts = postPage.getContent();

                for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);
                        resultActions
                                        .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                                        .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getCreateDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getModifyDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].authorId".formatted(i))
                                                        .value(post.getAuthor().getId()))
                                        .andExpect(jsonPath("$.items[%d].authorName".formatted(i))
                                                        .value(post.getAuthor().getName()))
                                        .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                                        .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                                        .andExpect(jsonPath("$.items[%d].published".formatted(i))
                                                        .value(post.isPublished()))
                                        .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
                }
        }

        @Test
        @DisplayName("다건 조회 with searchKeyword=생성")
        void t16() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts?page=1&pageSize=3&searchKeyword=생성"))
                                .andDo(print());

                Page<Post> postPage = postService
                                .findByListedPaged(true, "title", "생성", 1, 3);

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("items"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                                .andExpect(jsonPath("$.currentPageNumber").value(1))
                                .andExpect(jsonPath("$.pageSize").value(3));

                List<Post> posts = postPage.getContent();

                for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);
                        resultActions
                                        .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                                        .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getCreateDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getModifyDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].authorId".formatted(i))
                                                        .value(post.getAuthor().getId()))
                                        .andExpect(jsonPath("$.items[%d].authorName".formatted(i))
                                                        .value(post.getAuthor().getName()))
                                        .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                                        .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                                        .andExpect(jsonPath("$.items[%d].published".formatted(i))
                                                        .value(post.isPublished()))
                                        .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
                }
        }

        @Test
        @DisplayName("다건 조회 with searchKeywordType=content&searchKeyword=완료")
        void t17() throws Exception {
                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts?page=1&pageSize=3&searchKeywordType=content&searchKeyword=완료"))
                                .andDo(print());

                Page<Post> postPage = postService.findByListedPaged(true, "content", "완료", 1, 3);

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("items"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                                .andExpect(jsonPath("$.currentPageNumber").value(1))
                                .andExpect(jsonPath("$.pageSize").value(3));

                List<Post> posts = postPage.getContent();

                for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);
                        resultActions
                                        .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                                        .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getCreateDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getModifyDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].authorId".formatted(i))
                                                        .value(post.getAuthor().getId()))
                                        .andExpect(jsonPath("$.items[%d].authorName".formatted(i))
                                                        .value(post.getAuthor().getName()))
                                        .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                                        .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                                        .andExpect(jsonPath("$.items[%d].published".formatted(i))
                                                        .value(post.isPublished()))
                                        .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
                }
        }

        @Test
        @DisplayName("내글 다건 조회")
        void t18() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts/mine?pageNumber=1&pageSize=3")
                                                                .header("Authorization", "Bearer " + actorAuthToken))
                                .andDo(print());

                Page<Post> postPage = postService
                                .findByAuthorPaged(actor, 1, 3);

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("mine"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                                .andExpect(jsonPath("$.currentPageNumber").value(1))
                                .andExpect(jsonPath("$.pageSize").value(3));

                List<Post> posts = postPage.getContent();

                for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);
                        resultActions
                                        .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                                        .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getCreateDate().toString().substring(0, 20))))
                                        .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getModifyDate().toString().substring(0, 20))))
                                        .andExpect(jsonPath("$.items[%d].authorId".formatted(i))
                                                        .value(post.getAuthor().getId()))
                                        .andExpect(jsonPath("$.items[%d].authorName".formatted(i))
                                                        .value(post.getAuthor().getName()))
                                        .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                                        .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                                        .andExpect(jsonPath("$.items[%d].published".formatted(i))
                                                        .value(post.isPublished()))
                                        .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
                }
        }

        @Test
        @DisplayName("내글 다건 내용 조회 with searchKeywordType=content&searchKeyword=18명")
        void t19() throws Exception {
                Member actor = memberService.findByUsername("user1").get();

                String actorAuthToken = memberService.getAuthToken(actor);

                ResultActions resultActions = mvc
                                .perform(
                                                get("/api/v1/posts/mine?pageNumber=1&pageSize=3&searchKeywordType=content&searchKeyword=작성 완료")
                                                                .header("Authorization", "Bearer " + actorAuthToken))
                                .andDo(print());

                Page<Post> postPage = postService
                                .findByAuthorPaged(actor, "content", "작성 완료", 1, 3);

                resultActions
                                .andExpect(handler().handlerType(ApiV1PostController.class))
                                .andExpect(handler().methodName("mine"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalItems").value(postPage.getTotalElements()))
                                .andExpect(jsonPath("$.totalPages").value(postPage.getTotalPages()))
                                .andExpect(jsonPath("$.currentPageNumber").value(1))
                                .andExpect(jsonPath("$.pageSize").value(3));

                List<Post> posts = postPage.getContent();

                for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);
                        resultActions
                                        .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(post.getId()))
                                        .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getCreateDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers
                                                        .startsWith(post.getModifyDate().toString().substring(0, 25))))
                                        .andExpect(jsonPath("$.items[%d].authorId".formatted(i))
                                                        .value(post.getAuthor().getId()))
                                        .andExpect(jsonPath("$.items[%d].authorName".formatted(i))
                                                        .value(post.getAuthor().getName()))
                                        .andExpect(jsonPath("$.items[%d].title".formatted(i)).value(post.getTitle()))
                                        .andExpect(jsonPath("$.items[%d].content".formatted(i)).doesNotExist())
                                        .andExpect(jsonPath("$.items[%d].published".formatted(i))
                                                        .value(post.isPublished()))
                                        .andExpect(jsonPath("$.items[%d].listed".formatted(i)).value(post.isListed()));
                }
        }
}