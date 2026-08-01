package com.skala.shop.dto.order;

import com.skala.shop.domain.order.OrderItem;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderItemResponse(
	@Schema(description = "삭제된 도서는 null(제목/가격은 스냅샷으로 보존됨)", example = "1") Long bookId,
	@Schema(example = "클린 코드") String title,
	@Schema(description = "주문 시점 스냅샷 가격", example = "30000") Integer price,
	@Schema(example = "2") Integer quantity,
	@Schema(example = "60000") Integer subtotal
) {

	/** Book이 삭제된 이후에도 title/price는 스냅샷으로 남아있고, bookId만 null이 될 수 있다. */
	public static OrderItemResponse from(OrderItem orderItem) {
		Long bookId = orderItem.getBook() != null ? orderItem.getBook().getId() : null;
		return new OrderItemResponse(
				bookId,
				orderItem.getBookTitle(),
				orderItem.getPrice(),
				orderItem.getQuantity(),
				orderItem.getSubtotal()
		);
	}
}
