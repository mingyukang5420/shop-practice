package com.skala.shop.dto.book;

import com.skala.shop.domain.book.Book;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookSummaryResponse(
	@Schema(example = "1") Long id,
	@Schema(example = "클린 코드") String title,
	@Schema(example = "로버트 마틴") String author,
	@Schema(example = "30000") Integer price,
	@Schema(example = "20") Integer stock,
	@Schema(example = "IT/프로그래밍") String categoryName
) {

	public static BookSummaryResponse from(Book book) {
		return new BookSummaryResponse(
				book.getId(),
				book.getTitle(),
				book.getAuthor(),
				book.getPrice(),
				book.getStock(),
				book.getCategory().getName()
		);
	}
}
