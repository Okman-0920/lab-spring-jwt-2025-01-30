package com.ll.springjwt20250130.domain.member.member.dto;

import java.time.LocalDateTime;

import com.ll.springjwt20250130.domain.member.member.entity.Member;

import lombok.Getter;


@Getter
public class MemberDto {
    private long id;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private String nickname;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.createDate = member.getCreateDate();
        this.modifyDate = member.getModifyDate();
        this.nickname = member.getNickname();
    }
}