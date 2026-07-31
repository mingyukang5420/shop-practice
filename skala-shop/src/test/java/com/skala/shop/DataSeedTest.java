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

	@Test
	void data_sql로_더미_회원_카테고리_도서가_시딩된다() {
		assertThat(memberRepository.findById(1L)).isPresent();
		assertThat(categoryRepository.findAll()).isNotEmpty();
		assertThat(bookRepository.findAll()).isNotEmpty();
	}

	@Test
	void 재고가_1_2권인_도서가_최소_1건_존재한다_재고초과_시나리오_수동_검증용() {
		boolean hasLowStockBook = bookRepository.findAll().stream()
				.anyMatch(book -> book.getStock() <= 2);

		assertThat(hasLowStockBook).isTrue();
	}
}
