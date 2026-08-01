package com.skala.shop.domain.order;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

/**
 * GlobalExceptionHandler가 500 대신 400으로 변환하는 대상인 PropertyReferenceException을
 * findAll(Pageable) 파생 쿼리(Admin/Order 목록 조회가 사용)에서 실제로 재현해 검증한다.
 * (BookRepository.search처럼 커스텀 @Query를 쓰는 경우는 InvalidDataAccessApiUsageException으로
 * 감싸져 던져지지만, 이 단순 파생 쿼리는 이 예외를 직접 던진다.)
 */
@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class OrderRepositorySortTest {

	@Autowired
	private OrderRepository orderRepository;

	// 존재하지 않는 필드명으로 정렬을 요청하면 Spring Data가 PropertyReferenceException을
	// 던지는지 검증한다(GlobalExceptionHandler가 이를 400으로 변환하는 전제 조건).
	@Test
	void 존재하지_않는_필드로_정렬하면_PropertyReferenceException을_던진다() {
		assertThatThrownBy(() -> orderRepository.findAll(PageRequest.of(0, 10, Sort.by("nonexistent"))))
				.isInstanceOf(PropertyReferenceException.class);
	}
}
