package com.skala.shop.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** 등록(POST)과 수정(PUT)이 동일한 스키마를 사용한다(API명세서 2절). */
public record BookRequest(
	@NotBlank(message = "제목은 필수입니다.")
	String title,

	@NotBlank(message = "저자는 필수입니다.")
	String author,

	@NotNull(message = "카테고리는 필수입니다.")
	Long categoryId,

	@NotNull(message = "가격은 필수입니다.")
	@PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
	Integer price,

	@NotNull(message = "재고는 필수입니다.")
	@PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
	Integer stock,

	String description
) {
}
