package com.skala.shop.service;

import java.util.List;

import com.skala.shop.common.PageResponse;
import com.skala.shop.domain.book.Book;
import com.skala.shop.domain.cart.CartItem;
import com.skala.shop.domain.cart.CartItemRepository;
import com.skala.shop.domain.member.Member;
import com.skala.shop.domain.member.MemberRepository;
import com.skala.shop.domain.order.Order;
import com.skala.shop.domain.order.OrderItem;
import com.skala.shop.domain.order.OrderRepository;
import com.skala.shop.dto.order.OrderCreateRequest;
import com.skala.shop.dto.order.OrderItemResponse;
import com.skala.shop.dto.order.OrderResponse;
import com.skala.shop.dto.order.OrderSummaryResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

	/** MVP는 인증이 없어 모든 주문 요청이 고정 더미 회원(id=1)을 기준으로 동작한다(요구사항명세서 2절). */
	private static final Long DUMMY_MEMBER_ID = 1L;

	private final OrderRepository orderRepository;
	private final CartItemRepository cartItemRepository;
	private final MemberRepository memberRepository;

	public OrderService(
			OrderRepository orderRepository,
			CartItemRepository cartItemRepository,
			MemberRepository memberRepository
	) {
		this.orderRepository = orderRepository;
		this.cartItemRepository = cartItemRepository;
		this.memberRepository = memberRepository;
	}

	/**
	 * 재고 초과 주문 방지 3단계(담기→수정→주문생성) 중 마지막 검증 지점이다(기능명세서 5.1~5.3).
	 * 대상 항목을 먼저 전량 검증한 뒤에만 재고를 차감하므로, 하나라도 부족하면 어떤 것도 반영되지 않고
	 * 트랜잭션 전체가 롤백된다(TOCTOU 대응 재검증).
	 */
	@Transactional
	public OrderResponse createOrder(OrderCreateRequest request) {
		Member member = getMember();
		List<CartItem> targets = resolveTargets(member, request);

		if (targets.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "주문할 장바구니 항목이 없습니다.");
		}

		for (CartItem item : targets) {
			Book book = item.getBook();
			if (item.getQuantity() > book.getStock()) {
				throw BusinessException.insufficientStock(book.getTitle(), item.getQuantity(), book.getStock());
			}
		}

		Order order = new Order(member);
		for (CartItem item : targets) {
			Book book = item.getBook();
			book.decreaseStock(item.getQuantity());
			order.addOrderItem(OrderItem.snapshotOf(book, item.getQuantity()));
		}
		orderRepository.save(order);
		cartItemRepository.deleteAll(targets);

		return toResponse(order);
	}

	public PageResponse<OrderSummaryResponse> findMyOrders(Pageable pageable) {
		Member member = getMember();
		return PageResponse.from(orderRepository.findAllByMember(member, pageable).map(OrderSummaryResponse::from));
	}

	public OrderResponse findMyOrder(Long orderId) {
		Member member = getMember();
		Order order = orderRepository.findById(orderId)
				.filter(found -> found.getMember().getId().equals(member.getId()))
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
		return toResponse(order);
	}

	/** 관리자 조회는 회원 필터 없이 전체 주문을 대상으로 한다(API명세서 5절, 기능명세서 4.3). */
	public PageResponse<OrderSummaryResponse> findAllOrders(Pageable pageable) {
		return PageResponse.from(orderRepository.findAll(pageable).map(OrderSummaryResponse::from));
	}

	public OrderResponse findOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
		return toResponse(order);
	}

	/** cartItemIds 미지정 시 장바구니 전체, 지정 시 본인 소유 항목인지 확인한다(기능명세서 4.1). */
	private List<CartItem> resolveTargets(Member member, OrderCreateRequest request) {
		if (request == null || request.cartItemIds() == null) {
			return cartItemRepository.findAllByMember(member);
		}
		List<CartItem> items = cartItemRepository.findAllById(request.cartItemIds());
		boolean allOwnedByMember = items.stream().allMatch(item -> item.getMember().getId().equals(member.getId()));
		if (items.size() != request.cartItemIds().size() || !allOwnedByMember) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "존재하지 않거나 본인 소유가 아닌 장바구니 항목이 포함되어 있습니다.");
		}
		return items;
	}

	private OrderResponse toResponse(Order order) {
		List<OrderItemResponse> items = order.getOrderItems().stream()
				.map(OrderItemResponse::from)
				.toList();
		return new OrderResponse(order.getId(), order.getStatus().getDisplayName(), order.getTotalPrice(), items, order.getOrderedAt());
	}

	private Member getMember() {
		return memberRepository.findById(DUMMY_MEMBER_ID)
				.orElseThrow(() -> new IllegalStateException("더미 회원(id=1)이 시딩되어 있지 않습니다."));
	}
}
