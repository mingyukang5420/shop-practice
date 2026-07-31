package com.skala.shop.dto.book;

import java.time.LocalDateTime;

import com.skala.shop.domain.book.Book;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookResponse(
	@Schema(example = "1") Long id,
	@Schema(example = "클린 코드") String title,
	@Schema(example = "로버트 마틴") String author,
	@Schema(example = "3") Long categoryId,
	@Schema(example = "IT/프로그래밍") String categoryName,
	@Schema(example = "30000") Integer price,
	@Schema(example = "20") Integer stock,
	@Schema(example = "읽기 좋은 코드를 작성하는 방법에 대한 고전") String description,
	LocalDateTime createdAt
) {

	public static BookResponse from(Book book) {
		return new BookResponse(
				book.getId(),
				book.getTitle(),
				book.getAuthor(),
				book.getCategory().getId(),
				book.getCategory().getName(),
				book.getPrice(),
				book.getStock(),
				book.getDescription(),
				book.getCreatedAt()
		);
	}
}
