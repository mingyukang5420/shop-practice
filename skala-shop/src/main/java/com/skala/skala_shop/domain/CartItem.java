package com.skala.skala_shop.domain;

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
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	private Integer quantity;

	@Builder
	public CartItem(Member member, Book book, Integer quantity) {
		this.member = member;
		this.book = book;
		this.quantity = quantity;
	}

	/** 동일 도서 재담기 시 수량 합산 규칙(기능명세서 3.1)에 사용. */
	public void increaseQuantity(int amount) {
		this.quantity += amount;
	}

	public void changeQuantity(int quantity) {
		this.quantity = quantity;
	}
}
