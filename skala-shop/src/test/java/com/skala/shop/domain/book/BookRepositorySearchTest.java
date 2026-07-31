package com.skala.shop.domain.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;

@DataJpaTest
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

	@Test
	void keyword와_categoryId가_모두_없으면_전체_도서를_반환한다() {
		var result = bookRepository.search(null, null, PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isEqualTo(3);
	}

	@Test
	void keyword는_title과_author에_대소문자_무관_부분일치한다() {
		var byTitle = bookRepository.search("코드", null, PageRequest.of(0, 10));
		var byAuthor = bookRepository.search("김영하", null, PageRequest.of(0, 10));

		assertThat(byTitle.getContent()).extracting(Book::getTitle).containsExactly("클린 코드");
		assertThat(byAuthor.getContent()).extracting(Book::getTitle).containsExactly("여행의 이유");
	}

	@Test
	void categoryId만_있으면_해당_카테고리로만_필터링한다() {
		var result = bookRepository.search(null, novel.getId(), PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isEqualTo(2);
	}

	@Test
	void keyword와_categoryId를_함께_주면_AND로_결합한다() {
		var result = bookRepository.search("킹", novel.getId(), PageRequest.of(0, 10));

		assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("칼리의 노래");
	}

	@Test
	void 존재하지_않는_categoryId는_에러_없이_빈_목록을_반환한다() {
		var result = bookRepository.search(null, 9999L, PageRequest.of(0, 10));

		assertThat(result.getTotalElements()).isZero();
	}
}
