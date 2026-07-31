package com.skala.shop.controller;

import com.skala.shop.common.LoginMember;
import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.order.OrderCreateRequest;
import com.skala.shop.dto.order.OrderResponse;
import com.skala.shop.dto.order.OrderSummaryResponse;
import com.skala.shop.service.OrderService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> create(
			@LoginMember Long memberId,
			@RequestBody(required = false) OrderCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(memberId, request));
	}

	@GetMapping
	public ResponseEntity<PageResponse<OrderSummaryResponse>> findAll(
			@LoginMember Long memberId,
			@PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(orderService.findMyOrders(memberId, pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@LoginMember Long memberId, @PathVariable Long id) {
		return ResponseEntity.ok(orderService.findMyOrder(memberId, id));
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<OrderResponse> cancel(@LoginMember Long memberId, @PathVariable Long id) {
		return ResponseEntity.ok(orderService.cancelOrder(memberId, id));
	}
}
