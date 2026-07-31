package com.skala.shop.dto.order;

import java.time.LocalDateTime;

import com.skala.shop.domain.order.Order;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderSummaryResponse(
	@Schema(example = "1") Long orderId,
	@Schema(description = "주문완료 또는 주문취소", example = "주문완료") String status,
	@Schema(example = "60000") Integer totalPrice,
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
