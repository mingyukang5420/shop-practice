package com.skala.shop.dto.order;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
	Long orderId,
	String status,
	Integer totalPrice,
	List<OrderItemResponse> items,
	LocalDateTime orderedAt
) {
}
