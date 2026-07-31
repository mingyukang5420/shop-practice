package com.skala.shop.dto.book;

import com.skala.shop.domain.book.Book;

public record BookSummaryResponse(
	Long id,
	String title,
	String author,
	Integer price,
	Integer stock,
	String categoryName
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
