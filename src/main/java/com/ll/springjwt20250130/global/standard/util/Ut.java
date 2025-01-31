package com.ll.springjwt20250130.global.standard.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
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
        public static String toString(Object obj) {
            return om.writeValueAsString(obj);
        }
    }

    public static class jwt {
        public static String toString(String secret, long expireSeconds, Map<String, Object> body) {
            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

            String jwt = Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

            return jwt;
        }

        public static boolean isValid(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            try {
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr);

            } catch (Exception e) {
                return false;
            }
            return true;
        }

        public static Map<String, Object> payload(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            try {
                return (Map<String, Object>) Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .getPayload();

            } catch (Exception e) {
                return null;
            }
        }
    }
}