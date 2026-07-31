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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CartService {

	private static final Logger log = LoggerFactory.getLogger(CartService.class);

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
	public CartItemResponse add(Long memberId, CartItemAddRequest request) {
		Member member = getMember(memberId);
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

		log.info("장바구니 담기: memberId={}, bookId={}, quantity={}", memberId, request.bookId(), combinedQuantity);
		return toResponse(cartItem, book);
	}

	/** price/stock은 스냅샷이 아니라 항상 최신 Book 값을 조회해 반환한다(기능명세서 3.2). */
	public CartResponse findMyCart(Long memberId) {
		Member member = getMember(memberId);
		List<CartItemResponse> items = cartItemRepository.findAllByMember(member).stream()
				.map(item -> toResponse(item, item.getBook()))
				.toList();
		int totalPrice = items.stream().mapToInt(item -> item.price() * item.quantity()).sum();
		return new CartResponse(items, totalPrice);
	}

	@Transactional
	public CartItemResponse changeQuantity(Long memberId, Long itemId, CartItemQuantityRequest request) {
		CartItem cartItem = getOwnedCartItem(memberId, itemId);
		validateStock(cartItem.getBook(), request.quantity());
		cartItem.changeQuantity(request.quantity());
		log.info("장바구니 수량 수정: memberId={}, itemId={}, quantity={}", memberId, itemId, request.quantity());
		return toResponse(cartItem, cartItem.getBook());
	}

	@Transactional
	public void remove(Long memberId, Long itemId) {
		cartItemRepository.delete(getOwnedCartItem(memberId, itemId));
		log.info("장바구니 항목 삭제: memberId={}, itemId={}", memberId, itemId);
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

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
	}

	private Book getBook(Long bookId) {
		return bookRepository.findById(bookId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
	}

	/** 다른 회원 소유의 항목은 존재하지 않는 것과 동일하게 취급한다(항목 존재 여부로 소유 여부를 추측하지 못하도록). */
	private CartItem getOwnedCartItem(Long memberId, Long itemId) {
		return cartItemRepository.findById(itemId)
				.filter(item -> item.getMember().getId().equals(memberId))
				.orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
	}
}
