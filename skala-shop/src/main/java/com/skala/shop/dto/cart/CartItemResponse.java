package com.skala.shop.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartItemResponse(
	@Schema(example = "1") Long itemId,
	@Schema(example = "1") Long bookId,
	@Schema(example = "클린 코드") String title,
	@Schema(example = "30000") Integer price,
	@Schema(example = "2") Integer quantity,
	@Schema(example = "20") Integer stock
) {
}
