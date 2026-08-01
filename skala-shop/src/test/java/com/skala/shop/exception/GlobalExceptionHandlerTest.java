package com.skala.shop.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	// BusinessException이 발생하면 ErrorCode에 정의된 HTTP 상태/코드로 올바르게 변환되는지 검증한다.
	@Test
	void BusinessException은_ErrorCode의_상태와_코드로_변환된다() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/books/999");

		ResponseEntity<ErrorResponse> response =
				handler.handleBusinessException(new BusinessException(ErrorCode.BOOK_NOT_FOUND), request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().code()).isEqualTo("BOOK_NOT_FOUND");
		assertThat(response.getBody().path()).isEqualTo("/api/books/999");
	}

	// 재고 초과 예외 메시지에 도서명/요청 수량/가용 재고가 모두 포함되는지 검증한다.
	@Test
	void 재고초과_예외는_도서명_요청수량_가용재고를_메시지에_포함한다() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/cart/items");

		ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
				BusinessException.insufficientStock("클린 코드", 5, 2), request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().code()).isEqualTo("INSUFFICIENT_STOCK");
		assertThat(response.getBody().message()).contains("클린 코드", "5", "2");
	}

	// 정렬 파라미터 오류로 인한 예외가 500이 아니라 400 VALIDATION_ERROR로 변환되는지 검증한다.
	@Test
	void 존재하지_않는_sort_필드명은_500이_아니라_400_VALIDATION_ERROR로_변환된다() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/books");

		ResponseEntity<ErrorResponse> response = handler.handleInvalidSort(
				new InvalidDataAccessApiUsageException("Sort expression '[\"string\"]: ASC' must only contain property references"),
				request
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
	}
}
