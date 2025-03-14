package com.ll.springjwt20250130.global.rq;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.global.security.SecurityUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// 이 Class는 Request/ response 를 추상화한 객체
// Request, Response, Cookie, Session 등을 다룬다
@RequestScope
@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final MemberService memberService;

    // 스프링 시큐리티가 이해하는 방식으로 강제 로그인 처리
    // 임시 함수
    public void setLogin(Member member) {
        UserDetails user = new SecurityUser(
            member.getId(),
            member.getUsername(),
            "",
            member.getAuthorities()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user,
            user.getPassword(),
            user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public Member getActor() {
        return Optional.ofNullable(
                SecurityContextHolder
                    .getContext()
                    .getAuthentication()
            )
            .map(Authentication::getPrincipal)
            .filter(principal -> principal instanceof UserDetails)
            .map(principal -> (UserDetails) principal)
            .map(UserDetails::getUsername)
            .flatMap(memberService::findByUsername)
            .orElse(null);
    }

    public void setCookie(String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .path("/")
            .domain("localhost")
            .sameSite("Strict")
            .secure(true)
            .httpOnly(true)
            .build();
        resp.addHeader("Set-Cookie", cookie.toString());
    }

    public String getCookieValue(String name) {
        return Optional
            .ofNullable(req.getCookies())
            .stream() // 1 ~ 0
            .flatMap(cookies -> Arrays.stream(cookies))
            .filter(cookie -> cookie.getName().equals(name))
            .map(cookie -> cookie.getValue())
            .findFirst()
            .orElse(null);
    }

    public void setHeader(String name, String value ) {
        resp.addHeader(name, value);
    }

    public String getHeader(String name) {
        return req.getHeader(name);
    }

    public void deleteCookie(String name) {
        ResponseCookie cookie = ResponseCookie.from(name, null)
            .path("/")
            .domain("localhost")
            .sameSite("Strict")
            .secure(true)
            .httpOnly(true)
            .maxAge(0)
            .build();

        resp.addHeader("Set-Cookie", cookie.toString());
    }
}
