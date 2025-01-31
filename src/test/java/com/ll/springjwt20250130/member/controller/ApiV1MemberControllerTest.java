package com.ll.springjwt20250130.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

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

import com.ll.springjwt20250130.domain.member.member.controller.ApiV1MemberController;
import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired // 테스트는 의존성 주입을 Autowired 를 사용하여 강제로 해야 함
    private MemberService memberService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc // mvc: MockMvc 객체를 통해 HTTP 요청을 수행
                // HTTP 요청을 실행하는 메서드
                .perform(
                        // POST 요청을 "/api~join" URL로 보낸다
                        post("/api/v1/members/join")
                                // content: HTTP 요청 본문에 Json 데이터를 추가하고, 회원 가입 시 필요한 정보를 json형태로 전달
                                .content(""" 
                                    {
                                        "username": "usernew",
                                        "password": "1234",
                                        "nickname": "무명"
                                    }                                   
                                    """.stripIndent()) // 문자열의 들여쓰기를 제거함
                                // HTTP 요청 본문의 데이터 타입을 설정
                                .contentType(
                                        // JSON 데이터를 전달하고, UTF-8 인코딩을 사용하는 것으로 지정
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Member member = memberService.findByUsername("usernew").get();

        resultActions // 테스트의 결과는 다음과 같기를 기대한다.
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName())))
                .andExpect(jsonPath("$.data").exists()) // 데이터가 json 응답에 존재하는지
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 25))))
                // Matchers: 일치하는지 확인해라 / startsWith 처음부터 25자까지
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()));
    }

    @Test
    @DisplayName("회원가입 without username, password, nickname")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .content("""
                                    {
                                        "username": "",
                                        "password": "",
                                        "nickname": ""
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions // 테스트의 결과는 다음과 같기를 기대한다.
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        nickname-NotBlank: must not be blank
                        password-NotBlank: must not be blank
                        username-NotBlank: must not be blank
                        """.stripIndent().trim()));
    }

    @Test
    @DisplayName("회원가입 시 이미 사용중인 username, 409")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .content("""
                                    {
                                        "username": "user1",
                                        "password": "1234",
                                        "nickname": "무명"
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());


        resultActions // 테스트의 결과는 다음과 같기를 기대한다.
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("해당 username은 이미 사용중입니다."));
    }

    @Test
    @DisplayName("로그인")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .content("""
                                    {
                                        "username": "user1",
                                        "password": "1234"
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        Member member = memberService.findByUsername("user1").get();

        resultActions 
            .andExpect(handler().handlerType(ApiV1MemberController.class))
            .andExpect(handler().methodName("login"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("%s님 환영합니다.".formatted(member.getName())))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.item").exists())
            .andExpect(jsonPath("$.data.item.id").value(member.getId()))
            .andExpect(jsonPath("$.data.apiKey").value(member.getApiKey()))
            .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("로그인, without username")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .content("""
                                    {
                                        "username": "",
                                        "password": "1234"
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("username-NotBlank: must not be blank"));
    }

    @Test
    @DisplayName("로그인, without password")
    void t6() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .content("""
                                    {
                                        "username": "user1",
                                        "password": ""
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("password-NotBlank: must not be blank"));
    }

    @Test
    @DisplayName("로그인, with wrong username")
    void t7() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .content("""
                                    {
                                        "username": "user0",
                                        "password": "1234"
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("로그인, with wrong password")
    void t8() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/login")
                                .content("""
                                    {
                                        "username": "user1",
                                        "password": "1"
                                    }
                                    """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-2"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("내 정보, with user1")
    void t9() throws Exception {
        Member member = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer " + member.getApiKey())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()));
    }

    @Test
    @DisplayName("내 정보, with user2")
    void t10() throws Exception {
        Member member = memberService.findByUsername("user2").get();
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer " + member.getApiKey())
                )
                .andDo(print());
        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 25))))
                .andExpect(jsonPath("$.nickname").value(member.getNickname()));
    }

    @Test
    @DisplayName("내 정보 조회 with 잘못된 API KEY")
    void t11() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me")
                                .header("Authorization", "Bearer wrong-api-key")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."));
    }
}