package com.skala.skala_shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MVP는 카테고리 CRUD API를 제공하지 않고 {@code data.sql}로만 시딩하므로 id를 자동 생성하지 않는다.
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

	@Id
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	public Category(Long id, String name) {
		this.id = id;
		this.name = name;
	}
}
