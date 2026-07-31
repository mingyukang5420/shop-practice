package com.skala.shop.service;

import java.util.List;

import com.skala.shop.domain.book.Book;
import com.skala.shop.domain.book.BookRepository;
import com.skala.shop.domain.cart.CartItem;
import com.skala.shop.domain.cart.CartItemRepository;
import com.skala.shop.domain.member.Member;
import com.skala.shop.domain.member.MemberRepository;
import com.skala.shop.dto.cart.CartItemAddRequest;
import com.skala.shop.dto.cart.CartItemQuantityRequest;
import com.skala.shop.dto.cart.CartItemResponse;
import com.skala.shop.dto.cart.CartResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CartService {

	/** MVP는 인증이 없어 모든 장바구니 요청이 고정 더미 회원(id=1)을 기준으로 동작한다(요구사항명세서 2절). */
	private static final Long DUMMY_MEMBER_ID = 1L;

	private final CartItemRepository cartItemRepository;
	private final BookRepository bookRepository;
	private final MemberRepository memberRepository;

	public CartService(
			CartItemRepository cartItemRepository,
			BookRepository bookRepository,
			MemberRepository memberRepository
	) {
		this.cartItemRepository = cartItemRepository;
		this.bookRepository = bookRepository;
		this.memberRepository = memberRepository;
	}

	/** 동일 도서 재담기 시 신규 행 대신 수량을 합산하고, 합산된 총수량 기준으로 재고를 검증한다(기능명세서 3.1). */
	@Transactional
	public CartItemResponse add(CartItemAddRequest request) {
		Member member = getMember();
		Book book = getBook(request.bookId());
		CartItem cartItem = cartItemRepository.findByMemberAndBook(member, book).orElse(null);

		int combinedQuantity = (cartItem == null ? 0 : cartItem.getQuantity()) + request.quantity();
		validateStock(book, combinedQuantity);

		if (cartItem == null) {
			cartItem = CartItem.builder().member(member).book(book).quantity(combinedQuantity).build();
			cartItemRepository.save(cartItem);
		} else {
			cartItem.increaseQuantity(request.quantity());
		}

		return toResponse(cartItem, book);
	}

	/** price/stock은 스냅샷이 아니라 항상 최신 Book 값을 조회해 반환한다(기능명세서 3.2). */
	public CartResponse findMyCart() {
		Member member = getMember();
		List<CartItemResponse> items = cartItemRepository.findAllByMember(member).stream()
				.map(item -> toResponse(item, item.getBook()))
				.toList();
		int totalPrice = items.stream().mapToInt(item -> item.price() * item.quantity()).sum();
		return new CartResponse(items, totalPrice);
	}

	@Transactional
	public CartItemResponse changeQuantity(Long itemId, CartItemQuantityRequest request) {
		CartItem cartItem = getCartItem(itemId);
		validateStock(cartItem.getBook(), request.quantity());
		cartItem.changeQuantity(request.quantity());
		return toResponse(cartItem, cartItem.getBook());
	}

	@Transactional
	public void remove(Long itemId) {
		cartItemRepository.delete(getCartItem(itemId));
	}

	private void validateStock(Book book, int requestedQuantity) {
		if (requestedQuantity > book.getStock()) {
			throw BusinessException.insufficientStock(book.getTitle(), requestedQuantity, book.getStock());
		}
	}

	private CartItemResponse toResponse(CartItem cartItem, Book book) {
		return new CartItemResponse(
				cartItem.getId(), book.getId(), book.getTitle(), book.getPrice(), cartItem.getQuantity(), book.getStock());
	}

	private Member getMember() {
		return memberRepository.findById(DUMMY_MEMBER_ID)
				.orElseThrow(() -> new IllegalStateException("더미 회원(id=1)이 시딩되어 있지 않습니다."));
	}

	private Book getBook(Long bookId) {
		return bookRepository.findById(bookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
	}

	private CartItem getCartItem(Long itemId) {
		return cartItemRepository.findById(itemId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
	}
}
