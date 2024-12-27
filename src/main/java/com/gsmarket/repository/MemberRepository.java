package com.gsmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gsmarket.entity.MemberEntity;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    /**
     * 이 메소드는 입력받은 회원 아이디에 해당하는 MemberEntity 객체를 조회하는 기능을 수행합니다.
     */
    Optional<MemberEntity> findByMemberId(String memberId);
    
    Optional<MemberEntity> findByMemberEmail(String memberEmail);
    Optional<MemberEntity> findByMemberIdAndMemberEmail(String memberId, String memberEmail);
    
}
