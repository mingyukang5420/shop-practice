package com.skala.shop.controller;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.book.BookRequest;
import com.skala.shop.dto.book.BookResponse;
import com.skala.shop.dto.book.BookSummaryResponse;
import com.skala.shop.service.BookService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	public ResponseEntity<PageResponse<BookSummaryResponse>> findAll(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(bookService.findAll(keyword, categoryId, pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.findById(id));
	}

	@PostMapping
	public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return ResponseEntity.ok(bookService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		bookService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
