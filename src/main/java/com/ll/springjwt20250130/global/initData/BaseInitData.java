package com.ll.springjwt20250130.global.initData;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.domain.post.post.entity.Post;
import com.ll.springjwt20250130.domain.post.post.service.PostService;
import com.ll.springjwt20250130.global.app.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final MemberService memberService;
    private final PostService postService;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
        };
    }

    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;

        Member memberSystem = memberService.join("system", "1234", "시스템");
        if (AppConfig.isNotProd()) memberSystem.setApiKey("system");

        Member memberAdmin = memberService.join("admin", "1234", "관리자");
        if (AppConfig.isNotProd()) memberAdmin.setApiKey("admin");

        Member memberUser1 = memberService.join("user1", "1234", "유저1");
        if (AppConfig.isNotProd()) memberUser1.setApiKey("user1");

        Member memberUser2 = memberService.join("user2", "1234", "유저2");
        if (AppConfig.isNotProd()) memberUser2.setApiKey("user2");

        Member memberUser3 = memberService.join("user3", "1234", "유저3");
        if (AppConfig.isNotProd()) memberUser3.setApiKey("user3");

        Member memberUser4 = memberService.join("user4", "1234", "유저4");
        if (AppConfig.isNotProd()) memberUser4.setApiKey("user4");

        Member memberUser5 = memberService.join("user5", "1234", "유저5");
        if (AppConfig.isNotProd()) memberUser5.setApiKey("user5");
    }

    @Transactional
    public void work2() {
        if (postService.count() > 0)
            return;

        Member memberUser1 = memberService.findByUsername("user1").get();
        Member memberUser2 = memberService.findByUsername("user2").get();
        Member memberUser3 = memberService.findByUsername("user3").get();
        Member memberUser4 = memberService.findByUsername("user4").get();

        Post post1 = postService.write(
                memberUser1,
                "글1 생성",
                "글1, 내용",
                true,
                true
        );
        post1.addComment(memberUser2, "글1, 유저2(userId - 4), 댓글1");
        post1.addComment(memberUser3, "글1, 유저3(userId - 5), 댓글2");

        Post post2 = postService.write(
                memberUser1,
                "글2 생성",
                "글2, 내용 작성 완료",
                true,
                true
        );
        post1.addComment(memberUser4, "글2, 유저4(userId - 6), 댓글1");

        Post post3 = postService.write(
                memberUser2,
                "글3 생성",
                "글3, 내용",
                true,
                true
        );

        Post post4 = postService.write(
                memberUser3,
                "글4 생성",
                "글4, 내용 작성 완료",
                true,
                true
        );

        Post post5 = postService.write(
                memberUser4,
                "글5",
                "글5, 내용 작성 완료",
                true,
                true
        );

        Post post6 = postService.write(
                memberUser4,
                "글6",
                "글6, 내용 작성 완료, 공개x, 검색x",
                false,
                false
        );

        Post post7 = postService.write(
                memberUser4,
                "글7",
                "글7, 내용, 공개o, 검색x",
                true,
                false
        );

        Post post8 = postService.write(
                memberUser4,
                "글8",
                "글8, 내용 작성 완료, 공개x, 검색O",
                false,
                true
        );
    }
}
