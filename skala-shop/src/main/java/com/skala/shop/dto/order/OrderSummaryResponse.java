package com.skala.shop.dto.order;

import java.time.LocalDateTime;

import com.skala.shop.domain.order.Order;

public record OrderSummaryResponse(
	Long orderId,
	String status,
	Integer totalPrice,
	LocalDateTime orderedAt
) {

	public static OrderSummaryResponse from(Order order) {
		return new OrderSummaryResponse(
				order.getId(),
				order.getStatus().getDisplayName(),
				order.getTotalPrice(),
				order.getOrderedAt()
		);
	}
}
