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

	@Autowired
	private CartService cartService;

	@Test
	void 동일_도서를_재담기하면_새_행_대신_수량이_합산된다() {
		CartItemResponse first = cartService.add(new CartItemAddRequest(1L, 2));
		CartItemResponse second = cartService.add(new CartItemAddRequest(1L, 3));

		assertThat(second.itemId()).isEqualTo(first.itemId());
		assertThat(second.quantity()).isEqualTo(5);
		assertThat(cartService.findMyCart().items()).hasSize(1);
	}

	@Test
	void 합산_수량이_재고를_초과하면_INSUFFICIENT_STOCK을_던진다() {
		// 태백산맥 1(id=5)은 재고 1권
		assertThatThrownBy(() -> cartService.add(new CartItemAddRequest(5L, 2)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));
	}

	@Test
	void 존재하지_않는_도서를_담으면_BOOK_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> cartService.add(new CartItemAddRequest(9999L, 1)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND));
	}

	@Test
	void 수량_수정시_재고를_초과하면_INSUFFICIENT_STOCK을_던진다() {
		CartItemResponse item = cartService.add(new CartItemAddRequest(1L, 1));

		assertThatThrownBy(() -> cartService.changeQuantity(item.itemId(), new CartItemQuantityRequest(9999)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));
	}

	@Test
	void 존재하지_않는_항목_수정_삭제시_CART_ITEM_NOT_FOUND를_던진다() {
		assertThatThrownBy(() -> cartService.changeQuantity(9999L, new CartItemQuantityRequest(1)))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
		assertThatThrownBy(() -> cartService.remove(9999L))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
	}

	@Test
	void 항목을_삭제하면_장바구니에서_사라진다() {
		CartItemResponse item = cartService.add(new CartItemAddRequest(2L, 1));

		cartService.remove(item.itemId());

		assertThat(cartService.findMyCart().items())
				.noneMatch(response -> response.itemId().equals(item.itemId()));
	}
}
