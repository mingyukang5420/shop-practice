package com.skala.shop.service;

import com.skala.shop.common.PageResponse;
import com.skala.shop.domain.book.Book;
import com.skala.shop.domain.book.BookRepository;
import com.skala.shop.domain.book.Category;
import com.skala.shop.domain.book.CategoryRepository;
import com.skala.shop.dto.book.BookRequest;
import com.skala.shop.dto.book.BookResponse;
import com.skala.shop.dto.book.BookSummaryResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

	private static final Logger log = LoggerFactory.getLogger(BookService.class);

	private final BookRepository bookRepository;
	private final CategoryRepository categoryRepository;

	public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
		this.bookRepository = bookRepository;
		this.categoryRepository = categoryRepository;
	}

	public PageResponse<BookSummaryResponse> findAll(String keyword, Long categoryId, Pageable pageable) {
		return PageResponse.from(bookRepository.search(keyword, categoryId, pageable).map(BookSummaryResponse::from));
	}

	public BookResponse findById(Long id) {
		return BookResponse.from(getBook(id));
	}

	@Transactional
	public BookResponse create(BookRequest request) {
		Category category = getCategory(request.categoryId());
		Book book = Book.builder()
				.title(request.title())
				.author(request.author())
				.category(category)
				.price(request.price())
				.stock(request.stock())
				.description(request.description())
				.build();
		BookResponse response = BookResponse.from(bookRepository.save(book));
		log.info("도서 등록: title={}, price={}, stock={}", request.title(), request.price(), request.stock());
		return response;
	}

	@Transactional
	public BookResponse update(Long id, BookRequest request) {
		Book book = getBook(id);
		Category category = getCategory(request.categoryId());
		book.update(request.title(), request.author(), category, request.price(), request.stock(), request.description());
		log.info("도서 수정: id={}, title={}", id, request.title());
		return BookResponse.from(book);
	}

	/** 이미 주문된 도서도 삭제 가능하며, OrderItem의 스냅샷은 Book FK의 ON DELETE SET NULL로 보존된다. */
	@Transactional
	public void delete(Long id) {
		Book book = getBook(id);
		bookRepository.delete(book);
		log.info("도서 삭제: id={}, title={}", id, book.getTitle());
	}

	private Book getBook(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
	}

	private Category getCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
	}
}
