package com.skala.skala_shop.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

/** title/price는 주문 시점 스냅샷이라 이후 Book이 수정·삭제되어도 과거 주문 내역이 변하지 않는다(기능명세서 2.5, 4.1). */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	/** Book이 삭제되면 스냅샷 필드는 유지한 채 참조만 NULL 처리된다(DB의 ON DELETE SET NULL). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id")
	@OnDelete(action = OnDeleteAction.SET_NULL)
	private Book book;

	@Column(nullable = false)
	private String bookTitle;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer quantity;

	private OrderItem(Book book, Integer quantity) {
		this.book = book;
		this.bookTitle = book.getTitle();
		this.price = book.getPrice();
		this.quantity = quantity;
	}

	/** 주문 생성 시점의 도서명/가격을 스냅샷으로 고정해 OrderItem을 만든다. */
	public static OrderItem snapshotOf(Book book, int quantity) {
		return new OrderItem(book, quantity);
	}

	void assignOrder(Order order) {
		this.order = order;
	}

	public int getSubtotal() {
		return price * quantity;
	}
}
