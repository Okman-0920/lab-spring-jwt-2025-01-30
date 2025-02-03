package com.ll.springjwt20250130.global.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.global.rq.Rq;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	private final MemberService memberService;
	private final Rq rq;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// /api/로 시작하는 요청이 아니라면 패스
		// 일반적인 /home, /about 등 굳이 인증을 하지 않아도 돌아가도 되는 사이트는 그냥 지나치라는 것임
		if (!request.getRequestURI().contains("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String authorization = request.getHeader("Authorization");

		// Authorization 헤더가 없거나 Bearer 로 시작하지 않는다면 패스
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorization.substring("Bearer ".length());
		String[] tokenBits = token.split(" ", 2);

		if (tokenBits.length != 2) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = tokenBits[0];
		String accessToken = tokenBits[1];

		Member member = memberService.getMemberFromAccessToken(accessToken);

		if (member == null) {
			Optional<Member> opMemberByApiKey = memberService.findByApiKey(apiKey);

			if (opMemberByApiKey.isEmpty()) {
				filterChain.doFilter(request, response);
				return;
			}

			member = opMemberByApiKey.get();

			String newAccessToken = memberService.genAccessToken(member);

			response.setHeader("Authorization", "Bearer " + apiKey + " " + newAccessToken);
		}

		rq.setLogin(member);

		filterChain.doFilter(request, response);
	}
}
