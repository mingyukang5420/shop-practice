package com.skala.shop.dto.order;

import com.skala.shop.domain.order.OrderItem;

public record OrderItemResponse(
	Long bookId,
	String title,
	Integer price,
	Integer quantity,
	Integer subtotal
) {

	/** Book이 삭제된 이후에도 title/price는 스냅샷으로 남아있고, bookId만 null이 될 수 있다(기능명세서 2.5). */
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
