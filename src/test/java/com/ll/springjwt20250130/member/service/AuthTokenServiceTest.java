package com.ll.springjwt20250130.member.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ll.springjwt20250130.domain.member.member.service.AuthTokenService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {
	@Autowired
	private AuthTokenService authTokenService;

	@Test
	@DisplayName("authTokenService 서비스가 존재한다")
	void t1() {
		assertThat(authTokenService).isNotNull();
	}
}
