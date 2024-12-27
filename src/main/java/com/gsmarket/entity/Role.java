package com.gsmarket.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

   MEMBER("ROLE_MEMBER", "멤버"),
   ADMIN("ROLE_ADMIN", "관리자");
   
   private final String key;
   private final String title;
   
}