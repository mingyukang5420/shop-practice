package com.skala.shop.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 세션에 저장된 로그인 회원 id를 컨트롤러 메서드 파라미터로 주입받기 위한 어노테이션. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
}
