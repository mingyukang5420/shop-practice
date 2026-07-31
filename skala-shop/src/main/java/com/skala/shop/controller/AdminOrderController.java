package com.skala.shop.controller;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.order.OrderResponse;
import com.skala.shop.dto.order.OrderSummaryResponse;
import com.skala.shop.service.OrderService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MVP는 인증이 없어 {@code /api/orders}와 실질적 접근 차이가 없지만, 엔드포인트를 분리해두어
 * 추후 인증 도입 시 이 컨트롤러에만 ADMIN 권한 검사를 추가할 수 있도록 한다(기능명세서 4.3).
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public ResponseEntity<PageResponse<OrderSummaryResponse>> findAll(
			@PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(orderService.findAllOrders(pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.findOrder(id));
	}
}
