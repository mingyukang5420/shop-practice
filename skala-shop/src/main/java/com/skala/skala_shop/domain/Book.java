package com.skala.skala_shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer stock;

	@Column(length = 2000)
	private String description;

	@Builder
	public Book(String title, String author, Category category, Integer price, Integer stock, String description) {
		this.title = title;
		this.author = author;
		this.category = category;
		this.price = price;
		this.stock = stock;
		this.description = description;
	}

	/** PUT 수정: 등록과 동일한 스키마로 전체 필드를 교체한다. */
	public void update(String title, String author, Category category, Integer price, Integer stock, String description) {
		this.title = title;
		this.author = author;
		this.category = category;
		this.price = price;
		this.stock = stock;
		this.description = description;
	}

	/** 호출 전 재고 검증(REQ-017)은 서비스 계층 책임이며, 이 메서드는 검증된 수량만 넘겨받는다고 가정한다. */
	public void decreaseStock(int quantity) {
		this.stock -= quantity;
	}
}
