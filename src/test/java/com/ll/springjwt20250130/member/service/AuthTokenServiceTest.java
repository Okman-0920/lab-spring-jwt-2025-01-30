package com.ll.springjwt20250130.member.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.domain.member.member.service.AuthTokenService;
import com.ll.springjwt20250130.domain.member.member.service.MemberService;
import com.ll.springjwt20250130.global.standard.util.Ut;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {
	@Autowired
	private MemberService memberService;

	@Autowired
	private AuthTokenService authTokenService;

	// 테스트용 토큰 만료기간 : 1년
	int expireSeconds = 60 * 60 * 24 * 365;
	// 테스트용 토큰 시크릿 키
	private String secret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";

	@Test
	@DisplayName("authTokenService 서비스가 존재한다")
	void t1() {
		assertThat(authTokenService).isNotNull();
	}

	@Test
	@DisplayName("jjwt 로 JWT 생성, {name=\"Paul\", age=23}")
	void t2() {
		// 클레임 (토큰에 담을 데이터)
		Claims claims = Jwts.claims()
			.add("name", "Paul")
			.add("age", 23)
			.build();

		Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

		// 토큰을 언제 만들었는지
		Date issuedAt = new Date();
		Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

		String jwt = Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(issuedAt)
			.setExpiration(expiration)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		assertThat(jwt).isNotBlank();

		System.out.println("jwt = " + jwt);
	}

	@Test
	@DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
	void t3() {
		String jwt = Ut.jwt.toString(secret, expireSeconds, Map.of("name","Paul","age",23));

		assertThat(jwt).isNotBlank();

		System.out.println("jwt = " + jwt);
	}

	@Test
	@DisplayName("authTokenService.getAccessToken(member);")
	void t4() {
		Member memberUser1 = memberService.findByUsername("user1").get();

		String accessToken = authTokenService.genAccessToken(memberUser1);

		assertThat(accessToken).isNotBlank();

		System.out.println("accessToken = " + accessToken);
	}
}
