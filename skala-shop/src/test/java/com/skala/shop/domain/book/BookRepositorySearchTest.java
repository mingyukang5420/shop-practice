package com.skala.shop.domain.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;

/** data.sql 시딩 데이터와 무관하게 자체 픽스처만으로 검증하기 위해 시딩을 끈다(고정 id를 직접 지정하는 테스트라 실데이터와 충돌 방지). */
@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class BookRepositorySearchTest {

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private EntityManager em;

	private Category novel;
	private Category essay;

	@BeforeEach
	void setUp() {
		novel = new Category(1L, "소설");
		essay = new Category(2L, "에세이");
		em.persist(novel);
		em.persist(essay);

		em.persist(Book.builder().title("클린 코드").author("로버트 마틴").category(novel).price(30000).stock(10).build());
		em.persist(Book.builder().title("칼리의 노래").author("스티븐 킹").category(novel).price(15000).stock(5).build());
		em.persist(Book.builder().title("여행의 이유").author("김영하").category(essay).price(14000).stock(3).build());
		em.flush();
	}

	// keyword/categoryId를 둘 다 주지 않으면 전체 도서가 조회되는지 검증한다.
	@Test
	void keyword와_categoryId가_모두_없으면_전체_도서를_반환한다() {
		var result = bookRepository.search(null, null, PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isEqualTo(3);
	}

	// keyword가 title/author 어느 쪽에 있든, 대소문자 구분 없이 부분일치로 검색되는지 검증한다.
	@Test
	void keyword는_title과_author에_대소문자_무관_부분일치한다() {
		var byTitle = bookRepository.search("코드", null, PageRequest.of(0, 10));
		var byAuthor = bookRepository.search("김영하", null, PageRequest.of(0, 10));

		assertThat(byTitle.getContent()).extracting(Book::getTitle).containsExactly("클린 코드");
		assertThat(byAuthor.getContent()).extracting(Book::getTitle).containsExactly("여행의 이유");
	}

	// categoryId만 지정하면 해당 카테고리의 도서만 필터링되는지 검증한다.
	@Test
	void categoryId만_있으면_해당_카테고리로만_필터링한다() {
		var result = bookRepository.search(null, novel.getId(), PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isEqualTo(2);
	}

	// keyword와 categoryId를 함께 지정하면 두 조건이 AND로 결합되는지 검증한다.
	@Test
	void keyword와_categoryId를_함께_주면_AND로_결합한다() {
		var result = bookRepository.search("킹", novel.getId(), PageRequest.of(0, 10));

		assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("칼리의 노래");
	}

	// 존재하지 않는 categoryId로 필터링해도 예외 없이 빈 목록이 반환되는지 검증한다.
	@Test
	void 존재하지_않는_categoryId는_에러_없이_빈_목록을_반환한다() {
		var result = bookRepository.search(null, 9999L, PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isZero();
	}
}
