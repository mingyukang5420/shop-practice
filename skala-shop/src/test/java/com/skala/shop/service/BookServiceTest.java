package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.dto.book.BookRequest;
import com.skala.shop.dto.book.BookResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BookServiceTest {

	@Autowired
	private BookService bookService;

	// 존재하지 않는 도서 id로 조회하면 BOOK_NOT_FOUND 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_도서_조회시_BOOK_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> bookService.findById(9999L))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND));
	}

	// 존재하지 않는 categoryId로 도서를 등록하면 CATEGORY_NOT_FOUND 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_categoryId로_등록시_CATEGORY_NOT_FOUND를_던진다() {
		BookRequest request = new BookRequest("제목", "저자", 9999L, 10000, 5, "설명");

		assertThatThrownBy(() -> bookService.create(request))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));
	}

	// 도서 등록→조회→수정→삭제 전체 흐름이 정상 동작하고, 삭제 후에는 조회가 실패하는지 검증한다.
	@Test
	void 도서를_등록_조회_수정_삭제할_수_있다() {
		BookRequest createRequest = new BookRequest("테스트 도서", "테스트 저자", 1L, 20000, 3, "설명");
		BookResponse created = bookService.create(createRequest);

		assertThat(created.id()).isNotNull();
		assertThat(bookService.findById(created.id()).title()).isEqualTo("테스트 도서");

		BookRequest updateRequest = new BookRequest("수정된 도서", "테스트 저자", 1L, 25000, 4, "수정된 설명");
		BookResponse updated = bookService.update(created.id(), updateRequest);

		assertThat(updated.title()).isEqualTo("수정된 도서");
		assertThat(updated.price()).isEqualTo(25000);

		bookService.delete(created.id());

		assertThatThrownBy(() -> bookService.findById(created.id())).isInstanceOf(BusinessException.class);
	}

	// 존재하지 않는 categoryId로 목록을 조회하면 빈 목록이 반환되는지 검증한다.
	@Test
	void 존재하지_않는_categoryId로_필터링하면_빈_목록을_반환한다() {
		var result = bookService.findAll(null, 9999L, PageRequest.of(0, 10));

		assertThat(result.content()).isEmpty();
	}
}
