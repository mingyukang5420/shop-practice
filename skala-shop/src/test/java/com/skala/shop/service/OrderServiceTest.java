package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.skala.shop.domain.book.Book;
import com.skala.shop.domain.book.BookRepository;
import com.skala.shop.domain.cart.CartItem;
import com.skala.shop.domain.cart.CartItemRepository;
import com.skala.shop.domain.member.Member;
import com.skala.shop.domain.member.MemberRepository;
import com.skala.shop.dto.cart.CartItemAddRequest;
import com.skala.shop.dto.cart.CartItemResponse;
import com.skala.shop.dto.order.OrderCreateRequest;
import com.skala.shop.dto.order.OrderResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired
	private OrderService orderService;

	@Autowired
	private CartService cartService;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	void 대상이_없으면_INVALID_REQUEST를_던진다() {
		assertThatThrownBy(() -> orderService.createOrder(null))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
	}

	@Test
	void 장바구니_전체를_주문하면_재고차감_스냅샷저장_장바구니삭제가_모두_일어난다() {
		cartService.add(new CartItemAddRequest(1L, 2)); // 클린 코드, 재고 20

		OrderResponse response = orderService.createOrder(null);

		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).title()).isEqualTo("클린 코드");
		assertThat(response.items().get(0).quantity()).isEqualTo(2);
		assertThat(response.totalPrice()).isEqualTo(60000);
		assertThat(response.status()).isEqualTo("주문완료");
		assertThat(bookRepository.findById(1L).orElseThrow().getStock()).isEqualTo(18);
		assertThat(cartService.findMyCart().items()).isEmpty();
	}

	@Test
	void 재고가_부족한_항목이_하나라도_있으면_아무것도_반영하지_않고_전체_실패한다() {
		cartService.add(new CartItemAddRequest(1L, 2)); // 클린 코드, 재고 20, 충분

		// 담은 뒤 다른 경로(동시 주문/재고 조정)로 재고가 줄어든 상황(TOCTOU)을 재현:
		// 태백산맥 1(id=5, 재고 1)을 재고보다 많은 수량으로 장바구니에 직접 구성한다.
		Member member = memberRepository.findById(1L).orElseThrow();
		Book taebaek = bookRepository.findById(5L).orElseThrow();
		cartItemRepository.save(CartItem.builder().member(member).book(taebaek).quantity(5).build());

		assertThatThrownBy(() -> orderService.createOrder(null))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));

		// 전량 실패 시 어떤 항목도 반영되지 않아야 한다(기능명세서 5.1~5.3).
		assertThat(bookRepository.findById(1L).orElseThrow().getStock()).isEqualTo(20);
		assertThat(cartItemRepository.findAllByMember(member)).hasSize(2);
	}

	@Test
	void cartItemIds로_일부만_주문할_수_있다() {
		CartItemResponse target = cartService.add(new CartItemAddRequest(1L, 1));
		cartService.add(new CartItemAddRequest(2L, 1));

		OrderResponse response = orderService.createOrder(new OrderCreateRequest(List.of(target.itemId())));

		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).bookId()).isEqualTo(1L);
		assertThat(cartService.findMyCart().items()).hasSize(1);
	}

	@Test
	void 존재하지_않는_cartItemId가_섞이면_INVALID_REQUEST를_던진다() {
		assertThatThrownBy(() -> orderService.createOrder(new OrderCreateRequest(List.of(9999L))))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
	}

	@Test
	void 존재하지_않는_주문_조회시_ORDER_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> orderService.findMyOrder(9999L))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND));
	}

	@Test
	void 주문_목록과_상세를_조회할_수_있다() {
		cartService.add(new CartItemAddRequest(1L, 1));
		OrderResponse created = orderService.createOrder(null);

		assertThat(orderService.findMyOrders(PageRequest.of(0, 10)).content())
				.extracting(summary -> summary.orderId())
				.contains(created.orderId());
		assertThat(orderService.findMyOrder(created.orderId()).orderId()).isEqualTo(created.orderId());
	}
}
