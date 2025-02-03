package com.ll.springjwt20250130.domain.controller;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletResponse;

public class BaseController {
	protected void setCookie(HttpServletResponse resp, String name, String value) {
		resp.addHeader(
			"set-CooKie",
			ResponseCookie.from(name, value)
				.path("/")
				.domain("localhost")
				.sameSite("Strict")
				.secure(true)
				.httpOnly(true)
				.build()
				.toString()
		);
	}

}
