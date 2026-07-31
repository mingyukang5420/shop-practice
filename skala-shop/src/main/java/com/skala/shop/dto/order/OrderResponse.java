package com.skala.shop.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderResponse(
	@Schema(example = "1") Long orderId,
	@Schema(description = "주문완료 또는 주문취소", example = "주문완료") String status,
	@Schema(example = "60000") Integer totalPrice,
	List<OrderItemResponse> items,
	LocalDateTime orderedAt
) {
}
