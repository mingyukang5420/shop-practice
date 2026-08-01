package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.dto.cart.CartItemAddRequest;
import com.skala.shop.dto.cart.CartItemQuantityRequest;
import com.skala.shop.dto.cart.CartItemResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CartServiceTest {

	private static final Long MEMBER_ID = 1L;

	@Autowired
	private CartService cartService;

	// 같은 도서를 다시 담으면 새 항목이 아니라 기존 항목의 수량이 합산되는지 검증한다.
	@Test
	void 동일_도서를_재담기하면_새_행_대신_수량이_합산된다() {
		CartItemResponse first = cartService.add(MEMBER_ID, new CartItemAddRequest(1L, 2));
		CartItemResponse second = cartService.add(MEMBER_ID, new CartItemAddRequest(1L, 3));

		assertThat(second.itemId()).isEqualTo(first.itemId());
		assertThat(second.quantity()).isEqualTo(5);
		assertThat(cartService.findMyCart(MEMBER_ID).items()).hasSize(1);
	}

	// 합산된 수량이 재고를 초과하면 INSUFFICIENT_STOCK 예외가 발생하는지 검증한다.
	@Test
	void 합산_수량이_재고를_초과하면_INSUFFICIENT_STOCK을_던진다() {
		// 태백산맥 1(id=5)은 재고 1권
		assertThatThrownBy(() -> cartService.add(MEMBER_ID, new CartItemAddRequest(5L, 2)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));
	}

	// 존재하지 않는 도서를 장바구니에 담으려 하면 BOOK_NOT_FOUND 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_도서를_담으면_BOOK_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> cartService.add(MEMBER_ID, new CartItemAddRequest(9999L, 1)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND));
	}

	// 장바구니 수량을 재고 초과로 수정하려 하면 INSUFFICIENT_STOCK 예외가 발생하는지 검증한다.
	@Test
	void 수량_수정시_재고를_초과하면_INSUFFICIENT_STOCK을_던진다() {
		CartItemResponse item = cartService.add(MEMBER_ID, new CartItemAddRequest(1L, 1));

		assertThatThrownBy(() -> cartService.changeQuantity(MEMBER_ID, item.itemId(), new CartItemQuantityRequest(9999)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));
	}

	// 존재하지 않는 장바구니 항목을 수정/삭제하려 하면 CART_ITEM_NOT_FOUND 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_항목_수정_삭제시_CART_ITEM_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> cartService.changeQuantity(MEMBER_ID, 9999L, new CartItemQuantityRequest(1)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
		assertThatThrownBy(() -> cartService.remove(MEMBER_ID, 9999L))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
	}

	// 다른 회원 소유의 장바구니 항목을 수정/삭제하려 하면, 존재하지 않는 항목과 동일하게
	// CART_ITEM_NOT_FOUND로 처리되는지 검증한다.
	@Test
	void 다른_회원_소유_항목은_존재하지_않는_것과_동일하게_취급한다() {
		CartItemResponse item = cartService.add(MEMBER_ID, new CartItemAddRequest(1L, 1));
		Long otherMemberId = 9999L;

		assertThatThrownBy(() -> cartService.changeQuantity(otherMemberId, item.itemId(), new CartItemQuantityRequest(2)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
		assertThatThrownBy(() -> cartService.remove(otherMemberId, item.itemId()))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
	}

	// 장바구니 항목을 삭제하면 이후 조회 목록에서 실제로 제외되는지 검증한다.
	@Test
	void 항목을_삭제하면_장바구니에서_사라진다() {
		CartItemResponse item = cartService.add(MEMBER_ID, new CartItemAddRequest(2L, 1));

		cartService.remove(MEMBER_ID, item.itemId());

		assertThat(cartService.findMyCart(MEMBER_ID).items())
				.noneMatch(response -> response.itemId().equals(item.itemId()));
	}
}
