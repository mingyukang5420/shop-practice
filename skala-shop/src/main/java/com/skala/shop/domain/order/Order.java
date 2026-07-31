package com.skala.shop.domain.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.skala.shop.domain.member.Member;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(nullable = false)
	private Integer totalPrice;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime orderedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<>();

	public Order(Member member) {
		this.member = member;
		this.status = OrderStatus.ORDERED;
		this.totalPrice = 0;
	}

	/** 양방향 연관관계를 유지하며 항목을 추가하고 총액을 누적한다. */
	public void addOrderItem(OrderItem orderItem) {
		orderItem.assignOrder(this);
		this.orderItems.add(orderItem);
		this.totalPrice += orderItem.getSubtotal();
	}

	public boolean isCanceled() {
		return this.status == OrderStatus.CANCELED;
	}

	/** 이미 취소된 주문인지 여부는 호출 전 Service 계층에서 확인한다고 가정한다. */
	public void cancel() {
		this.status = OrderStatus.CANCELED;
	}
}
