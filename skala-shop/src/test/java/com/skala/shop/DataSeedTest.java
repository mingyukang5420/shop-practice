package com.skala.shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.skala.shop.domain.book.BookRepository;
import com.skala.shop.domain.book.CategoryRepository;
import com.skala.shop.domain.member.MemberRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataSeedTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private BookRepository bookRepository;

	// data.sql 초기화 스크립트로 회원/카테고리/도서 더미 데이터가 정상적으로 시딩되는지 검증한다.
	@Test
	void data_sql로_더미_회원_카테고리_도서가_시딩된다() {
		assertThat(memberRepository.findById(1L)).isPresent();
		assertThat(categoryRepository.findAll()).isNotEmpty();
		assertThat(bookRepository.findAll()).isNotEmpty();
	}

	// 재고 초과 시나리오를 수동으로 재현할 수 있도록, 재고가 1~2권인 도서가 시딩 데이터에 최소 1건 있는지 검증한다.
	@Test
	void 재고가_1_2권인_도서가_최소_1건_존재한다_재고초과_시나리오_수동_검증용() {
		boolean hasLowStockBook = bookRepository.findAll().stream()
				.anyMatch(book -> book.getStock() <= 2);

		assertThat(hasLowStockBook).isTrue();
	}
}
