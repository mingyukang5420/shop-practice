package com.skala.shop.controller;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.book.BookRequest;
import com.skala.shop.dto.book.BookResponse;
import com.skala.shop.dto.book.BookSummaryResponse;
import com.skala.shop.exception.ErrorResponse;
import com.skala.shop.service.BookService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@Tag(name = "Book API", description = "도서 조회/등록/수정/삭제. 인증 없이 공개된다(등록/수정/삭제 포함, MVP 범위).")
@RestController
@RequestMapping("/api/books")
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@Operation(summary = "도서 목록 조회", description = "keyword(제목/저자 부분일치)와 categoryId로 필터링 가능하며, "
			+ "존재하지 않는 categoryId는 에러가 아니라 빈 목록을 반환한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공(페이지네이션)")
	})
	@GetMapping
	public ResponseEntity<PageResponse<BookSummaryResponse>> findAll(
			@Parameter(description = "제목/저자 부분 일치 검색어") @RequestParam(required = false) String keyword,
			@Parameter(description = "카테고리 id") @RequestParam(required = false) Long categoryId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(bookService.findAll(keyword, categoryId, pageable));
	}

	@Operation(summary = "도서 상세 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 도서", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":404,"code":"BOOK_NOT_FOUND","message":"도서를 찾을 수 없습니다.","path":"/api/books/999"}""")))
	})
	@GetMapping("/{id}")
	public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.findById(id));
	}

	@Operation(summary = "도서 등록")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "등록 성공"),
			@ApiResponse(responseCode = "400", description = "필수값 누락/형식 오류", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":400,"code":"VALIDATION_ERROR","message":"title: 제목은 필수입니다.","path":"/api/books"}"""))),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":404,"code":"CATEGORY_NOT_FOUND","message":"카테고리를 찾을 수 없습니다.","path":"/api/books"}""")))
	})
	@PostMapping
	public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
	}

	@Operation(summary = "도서 수정", description = "PUT 전체 필드 교체 방식(부분 수정 아님).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 도서 또는 카테고리"),
			@ApiResponse(responseCode = "400", description = "필수값 누락/형식 오류")
	})
	@PutMapping("/{id}")
	public ResponseEntity<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return ResponseEntity.ok(bookService.update(id, request));
	}

	@Operation(summary = "도서 삭제", description = "이미 주문된 도서도 삭제 가능하며, 과거 주문 내역의 도서명/가격은 "
			+ "스냅샷으로 보존되어 영향받지 않는다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공(본문 없음)"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 도서")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		bookService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
