package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.dto.cart.CartItemAddRequest;
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
class AdminOrderQueryTest {

	private static final Long MEMBER_ID = 1L;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CartService cartService;

	// 관리자용 주문 조회는 회원 필터 없이 전체 주문 목록/상세를 반환하는지 검증한다.
	@Test
	void 관리자_조회는_회원_필터_없이_전체_주문을_반환한다() {
		cartService.add(MEMBER_ID, new CartItemAddRequest(1L, 1));
		OrderResponse created = orderService.createOrder(MEMBER_ID, null);

		assertThat(orderService.findAllOrders(PageRequest.of(0, 10)).content())
				.extracting(summary -> summary.orderId())
				.contains(created.orderId());
		assertThat(orderService.findOrder(created.orderId()).orderId()).isEqualTo(created.orderId());
	}

	// 존재하지 않는 주문 id로 조회하면 ORDER_NOT_FOUND 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_주문_조회시_ORDER_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> orderService.findOrder(9999L))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND));
	}
}
