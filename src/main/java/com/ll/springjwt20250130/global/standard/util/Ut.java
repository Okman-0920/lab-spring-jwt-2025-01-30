package com.ll.springjwt20250130.global.standard.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;

public class Ut {
    public static class str {
        public static boolean isBlank (String str) {
            return str == null || str.trim().isEmpty();
            // trim: 문자열의 앞과 뒤에 있는 공백을 제거
            //  ㄴ 사용자의 실수로 발생한 공백도 빈 문자열 처리하여 유효성 검사를 수행하기 위함
            //  ㄴ 공백도 빈 문자열로 처리하기 위함
            // isEmpty(): 빈 문자열 인지를 확인 (!= null)
        }
    }

    public static class json {
        private static final ObjectMapper om = new ObjectMapper();

        @SneakyThrows
        public static String tostring(Object obj) {
            return om.writeValueAsString(obj);
        }
    }

    public static class jwt {
        public static String toString(String secret, int expireSeconds, Map<String, Object> body) {
            // claim = 토큰의 내용
            ClaimsBuilder claimsBuilder = Jwts.claims();

            for (Map.Entry<String, Object> entry : body.entrySet()) {
                claimsBuilder.add(entry.getKey(), entry.getValue());
            }

            Claims claims = claimsBuilder.build();
            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

            return jwt;
        }
    }
}
