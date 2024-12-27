package com.gsmarket.dto;

import lombok.*;

import java.time.LocalDateTime;

import com.gsmarket.entity.MemberEntity;
import com.gsmarket.entity.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class MemberDTO {
    private String memberId;
    private String memberPassword;
    private String memberName;
    private String memberEmail;
    private String memberPhoneNumber;
    private LocalDateTime memberRegDate;
    private Role role;
    

    //이 함수는 MemberEntity 객체를 입력으로 받아 MemberDTO 객체로 변환하는 기능을 수행합니다.
    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();

        memberDTO.setMemberId(memberEntity.getMemberId());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberPhoneNumber(memberEntity.getMemberPhoneNumber());
        memberDTO.setMemberRegDate(memberEntity.getMemberRegDate());
        memberDTO.setRole(memberEntity.getRole());
        return memberDTO;

    }

}
