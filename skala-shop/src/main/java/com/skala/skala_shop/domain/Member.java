package com.skala.skala_shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MVP에는 인증이 없어 id=1 고정 더미 회원만 존재한다({@code data.sql}로 시딩).
 * 회원가입 API가 없으므로 id는 자동 생성하지 않고 시딩 값을 그대로 사용한다.
 */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	private Long id;

	@Column(nullable = false)
	private String name;

	public Member(Long id, String name) {
		this.id = id;
		this.name = name;
	}
}
