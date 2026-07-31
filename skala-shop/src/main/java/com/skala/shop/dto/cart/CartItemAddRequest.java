package com.skala.shop.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemAddRequest(

	@NotNull(message = "도서 id는 필수입니다.")
	@Schema(example = "1")
	Long bookId,

	@NotNull(message = "수량은 필수입니다.")
	@Positive(message = "수량은 1 이상이어야 합니다.")
	@Schema(example = "2")
	Integer quantity
) {
}
