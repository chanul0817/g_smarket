package com.gsmarket.entity;

import lombok.*;

import org.hibernate.annotations.Cascade;

import com.gsmarket.dto.MemberDTO;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "member_table")
public class MemberEntity {
    @Id
    @Column(nullable = false, length = 30)
    private String memberId;

    @Column(nullable = false, length = 30)
    private String memberPassword;

    @Column(nullable = false, length = 30)
    private String memberName;

    @Column(nullable = false, length = 30)
    private String memberEmail;

    @Column(nullable = false, length = 30)
    private String memberPhoneNumber;

    @Column(name = "memberRegDate", updatable = false)
    private LocalDateTime memberRegDate;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Board> boards = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "like_board",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "board_mno"))
    private Set<Board> likedBoards = new HashSet<>();

    @ManyToMany(mappedBy = "likedBoards", cascade = CascadeType.ALL)
    private Set<MemberEntity> likedMembers = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Note> sentNotes = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    private List<Note> receivedNotes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @PrePersist
    public void memberRegDate() {
        this.memberRegDate = LocalDateTime.now();
    }

    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        if (memberDTO.getMemberPhoneNumber() == null || memberDTO.getMemberPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수 입력 항목입니다.");
        }

        return MemberEntity.builder()
                .memberId(memberDTO.getMemberId())
                .memberPassword(memberDTO.getMemberPassword())
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .memberPhoneNumber(memberDTO.getMemberPhoneNumber())
                .role(Role.MEMBER)
                .emailVerified(false)
                .build();
    }



    public static MemberEntity toUpdateMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();

        memberEntity.setMemberId(memberDTO.getMemberId());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPhoneNumber(memberDTO.getMemberPhoneNumber());
        memberEntity.setRole(Role.MEMBER);
        return memberEntity;
    }
}

