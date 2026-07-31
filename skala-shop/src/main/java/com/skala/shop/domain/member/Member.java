package com.skala.shop.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** password는 항상 BCrypt로 암호화된 값만 저장한다(평문 저장 금지). */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer point;

	public Member(String loginId, String password, String name, Integer point) {
		this.loginId = loginId;
		this.password = password;
		this.name = name;
		this.point = point;
	}

	/** 주문 생성 시 총액만큼 포인트를 차감한다. 잔액 검증은 Service 계층 책임이다. */
	public void usePoint(int amount) {
		this.point -= amount;
	}

	/** 주문 취소 시 총액만큼 포인트를 환급한다. */
	public void refundPoint(int amount) {
		this.point += amount;
	}
}
