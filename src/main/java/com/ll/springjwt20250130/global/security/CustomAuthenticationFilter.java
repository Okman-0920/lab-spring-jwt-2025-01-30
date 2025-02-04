package com.ll.springjwt20250130.global.security;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
	private final MemberService memberService;
	private final Rq rq;

	private String[] getAuthTokensFromRequest() {
		String authorization = rq.getHeader("Authorization");

		if (authorization != null && authorization.startsWith("Bearer ") {
			String token = authorization.substring("Bearer ".length());
			String[] tokenBits = token.split(" ",2);

			if (tokenBits.length == 2)
				return new String[]{tokenBits[0], tokenBits[1]};
		}

		String apiKey = rq.getCookieValue("apiKey");
		String accessToken = rq.getCookieValue("accessToken");

		if (apiKey != null && accessToken != null)
			return new String[]{apiKey, accessToken};

		return null;
	}

	private void refreshAccessToken(Member member) {
		String newAccessToken = memberService.genAccessToken(member);

		rq.setHeader("Authorization", "Bearer " + member.getApiKey() + " " + newAccessToken);
		rq.setCookie("accessToken", newAccessToken);
	}

	private Member refreshAccessTokenByApiKey(String apiKey) {
		Optional<Member> opMemberByApiKey = memberService.findByApiKey(apiKey);

		if (opMemberByApiKey.isEmpty()) {
			return null
		}

		Member member = opMemberByApiKey.get();

		refreshAccessToken(member);

		return member;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// /api/로 시작하는 요청이 아니라면 패스
		// 일반적인 /home, /about 등 굳이 인증을 하지 않아도 돌아가도 되는 사이트는 그냥 지나치라는 것임
		if (!request.getRequestURI().contains("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		if (List.of("/api/v1/members/login", "/api/v1/members/logout", "/api/v1/members/join").contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String[] authTokens = getAuthTokensFromRequest();

		String apiKey = authTokens[0];
		String accessToken = authTokens[1];

		Member member = memberService.getMemberFromAccessToken(accessToken);

		if (member == null)
			member = refreshAccessTokenByApiKey(apiKey);

		if (member != null)
			rq.setLogin(member);

		filterChain.doFilter(request, response);
	}
}
