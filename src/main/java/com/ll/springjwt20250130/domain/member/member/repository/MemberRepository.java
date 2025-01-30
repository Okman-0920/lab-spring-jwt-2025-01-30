package com.ll.springjwt20250130.domain.member.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.springjwt20250130.domain.member.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    Optional<Member> findByApiKey(String apiKey);
}
