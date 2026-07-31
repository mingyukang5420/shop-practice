package com.skala.shop.domain.book;

import static org.assertj.core.api.Assertions.assertThat;

import com.skala.shop.domain.member.Member;
import com.skala.shop.domain.order.Order;
import com.skala.shop.domain.order.OrderItem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class OrderItemBookDeletionTest {

	@Autowired
	private jakarta.persistence.EntityManager em;

	@Test
	void orderItem은_주문시점_스냅샷을_유지하고_book이_삭제되면_참조만_NULL이_된다() {
		Member member = new Member(1L, "더미 회원");
		Category category = new Category(1L, "소설");
		Book book = Book.builder()
				.title("클린 코드")
				.author("로버트 마틴")
				.category(category)
				.price(30000)
				.stock(10)
				.build();
		em.persist(member);
		em.persist(category);
		em.flush();

		em.persist(book);
		em.flush();

		Order order = new Order(member);
		order.addOrderItem(OrderItem.snapshotOf(book, 2));
		em.persist(order);
		em.flush();

		Long bookId = book.getId();
		Long orderItemId = order.getOrderItems().get(0).getId();

		// 영속성 컨텍스트를 비워, 실제 BookService.delete()처럼 OrderItem이 로드되지 않은
		// 새 세션에서 Book만 삭제하는 상황을 재현한다(같은 컨텍스트에 OrderItem이 남아있으면
		// Hibernate가 book 삭제 전 참조 무결성을 자체적으로 검사해 오탐 예외를 던진다).
		em.clear();
		em.remove(em.find(Book.class, bookId));
		em.flush();
		em.clear();

		OrderItem persisted = em.find(OrderItem.class, orderItemId);

		assertThat(persisted).isNotNull();
		assertThat(persisted.getBook()).isNull();
		assertThat(persisted.getBookTitle()).isEqualTo("클린 코드");
		assertThat(persisted.getPrice()).isEqualTo(30000);
		assertThat(persisted.getSubtotal()).isEqualTo(60000);
	}
}
