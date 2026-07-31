package com.skala.shop.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** 등록(POST)과 수정(PUT)이 동일한 스키마를 사용한다(API명세서 2절). */
public record BookRequest(
	@NotBlank(message = "제목은 필수입니다.")
	@Schema(example = "클린 코드")
	String title,

	@NotBlank(message = "저자는 필수입니다.")
	@Schema(example = "로버트 마틴")
	String author,

	@NotNull(message = "카테고리는 필수입니다.")
	@Schema(example = "3")
	Long categoryId,

	@NotNull(message = "가격은 필수입니다.")
	@PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
	@Schema(example = "30000")
	Integer price,

	@NotNull(message = "재고는 필수입니다.")
	@PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
	@Schema(example = "20")
	Integer stock,

	@Schema(example = "읽기 좋은 코드를 작성하는 방법에 대한 고전")
	String description
) {
}
