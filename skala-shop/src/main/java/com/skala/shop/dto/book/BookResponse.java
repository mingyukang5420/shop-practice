package com.skala.shop.dto.book;

import java.time.LocalDateTime;

import com.skala.shop.domain.book.Book;

public record BookResponse(
	Long id,
	String title,
	String author,
	Long categoryId,
	String categoryName,
	Integer price,
	Integer stock,
	String description,
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
