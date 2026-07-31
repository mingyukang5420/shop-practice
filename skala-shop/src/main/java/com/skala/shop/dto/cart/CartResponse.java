package com.skala.shop.dto.cart;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartResponse(
	List<CartItemResponse> items,
	@Schema(example = "60000") Integer totalPrice
) {
}
