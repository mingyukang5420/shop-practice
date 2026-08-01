package com.skala.shop.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

	// Spring Data의 Page 객체를 프로젝트 공통 페이징 응답(PageResponse)으로 변환할 때
	// content/page/size/totalElements/totalPages가 모두 올바르게 매핑되는지 검증한다.
	@Test
	void Spring_Data_Page를_공통_페이징_응답_포맷으로_변환한다() {
		var page = new PageImpl<>(List.of("클린 코드", "칼리의 노래"), PageRequest.of(0, 2), 5);

		PageResponse<String> response = PageResponse.from(page);

		assertThat(response.content()).containsExactly("클린 코드", "칼리의 노래");
		assertThat(response.page()).isZero();
		assertThat(response.size()).isEqualTo(2);
		assertThat(response.totalElements()).isEqualTo(5);
		assertThat(response.totalPages()).isEqualTo(3);
	}
}
