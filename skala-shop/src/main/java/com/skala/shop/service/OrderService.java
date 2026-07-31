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
	 * 재고와 포인트를 모두 먼저 검증한 뒤에만 반영하므로, 하나라도 부족하면 어떤 것도 반영되지 않고
	 * 트랜잭션 전체가 롤백된다(TOCTOU 대응 재검증 + 포인트 검증, 기능명세서 4.1).
	 */
	@Transactional
	public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
		Member member = getMember(memberId);
		List<CartItem> targets = resolveTargets(member, request);

		if (targets.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "주문할 장바구니 항목이 없습니다.");
		}

		int totalOrderPrice = 0;
		for (CartItem item : targets) {
			Book book = item.getBook();
			if (item.getQuantity() > book.getStock()) {
				throw BusinessException.insufficientStock(book.getTitle(), item.getQuantity(), book.getStock());
			}
			totalOrderPrice += book.getPrice() * item.getQuantity();
		}
		if (member.getPoint() < totalOrderPrice) {
			throw BusinessException.insufficientPoint(totalOrderPrice, member.getPoint());
		}

		Order order = new Order(member);
		for (CartItem item : targets) {
			Book book = item.getBook();
			book.decreaseStock(item.getQuantity());
			order.addOrderItem(OrderItem.snapshotOf(book, item.getQuantity()));
		}
		member.usePoint(totalOrderPrice);
		orderRepository.save(order);
		cartItemRepository.deleteAll(targets);

		return toResponse(order);
	}

	public PageResponse<OrderSummaryResponse> findMyOrders(Long memberId, Pageable pageable) {
		Member member = getMember(memberId);
		return PageResponse.from(orderRepository.findAllByMember(member, pageable).map(OrderSummaryResponse::from));
	}

	public OrderResponse findMyOrder(Long memberId, Long orderId) {
		Member member = getMember(memberId);
		return toResponse(getOwnedOrder(member, orderId));
	}

	/**
	 * 주문 전체를 취소한다(항목 단위 부분 취소는 지원하지 않음, 기능명세서 4.4).
	 * 삭제된 도서(FK가 NULL인 OrderItem)는 재고 복구 대상에서 제외한다.
	 */
	@Transactional
	public OrderResponse cancelOrder(Long memberId, Long orderId) {
		Member member = getMember(memberId);
		Order order = getOwnedOrder(member, orderId);
		if (order.isCanceled()) {
			throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELED);
		}

		for (OrderItem orderItem : order.getOrderItems()) {
			Book book = orderItem.getBook();
			if (book != null) {
				book.increaseStock(orderItem.getQuantity());
			}
		}
		member.refundPoint(order.getTotalPrice());
		order.cancel();

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

	private Member getMember(Long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
	}

	/** 다른 회원 소유의 주문은 존재하지 않는 것과 동일하게 취급한다. */
	private Order getOwnedOrder(Member member, Long orderId) {
		return orderRepository.findById(orderId)
				.filter(found -> found.getMember().getId().equals(member.getId()))
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
	}
}
