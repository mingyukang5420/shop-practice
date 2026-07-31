package com.skala.shop.controller;

import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.order.OrderResponse;
import com.skala.shop.dto.order.OrderSummaryResponse;
import com.skala.shop.exception.ErrorResponse;
import com.skala.shop.service.OrderService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 회원 필터 없이 전체 주문을 조회한다. 세션 인증이 도입된 이후에도(v1.1) 이 엔드포인트는
 * 그대로 인증 없이 공개 상태를 유지하며, 추후 관리자 권한 체계 도입 시 이 컨트롤러에만
 * ADMIN 권한 검사를 추가할 수 있도록 엔드포인트를 분리해두었다(기능명세서 4.3).
 */
@Tag(name = "Admin API", description = "전체 주문 목록/상세 조회(회원 필터 없음). 인증 없이 공개된다.")
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "전체 주문 목록 조회", description = "회원 필터 없이 모든 회원의 주문을 조회한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공(페이지네이션)")
	})
	@GetMapping
	public ResponseEntity<PageResponse<OrderSummaryResponse>> findAll(
			@PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(orderService.findAllOrders(pageable));
	}

	@Operation(summary = "주문 상세 조회", description = "회원 필터 없이 주문 소유자와 무관하게 조회 가능하다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 주문", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":404,"code":"ORDER_NOT_FOUND","message":"주문을 찾을 수 없습니다.","path":"/api/admin/orders/999"}""")))
	})
	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.findOrder(id));
	}
}
