package com.ll.springjwt20250130.domain.member.member.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ll.springjwt20250130.domain.member.member.entity.Member;
import com.ll.springjwt20250130.global.standard.util.Ut;

@Service
public class AuthTokenService {
	public String genAccessToken(Member member) {
		long id = member.getId();
		String username = member.getUsername();

		return Ut.jwt.toString("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890",
			60 * 60 * 24 * 365,
			Map.of("id", id, "username", username)
		);
	}
}
