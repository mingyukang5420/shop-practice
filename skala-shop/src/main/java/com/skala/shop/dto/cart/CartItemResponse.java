package com.skala.shop.dto.cart;

public record CartItemResponse(
	Long itemId,
	Long bookId,
	String title,
	Integer price,
	Integer quantity,
	Integer stock
) {
}
