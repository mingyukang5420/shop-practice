package com.skala.shop.controller;

import com.skala.shop.common.LoginMember;
import com.skala.shop.common.PageResponse;
import com.skala.shop.dto.order.OrderCreateRequest;
import com.skala.shop.dto.order.OrderResponse;
import com.skala.shop.dto.order.OrderSummaryResponse;
import com.skala.shop.exception.ErrorResponse;
import com.skala.shop.service.OrderService;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order API", description = "장바구니 기반 주문 생성/조회/취소. 세션 인증이 필요하며, "
		+ "미인증 요청은 401 UNAUTHORIZED를 반환한다.")
@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private static final String UNAUTHORIZED_EXAMPLE = """
			{"timestamp":"2026-07-31T09:00:00","status":401,"code":"UNAUTHORIZED","message":"로그인이 필요합니다.","path":"/api/orders"}""";

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "주문 생성", description = "요청 본문이 없으면 장바구니 전체를, cartItemIds를 지정하면 "
			+ "해당 항목만 주문으로 전환한다. 재고 검증을 모두 통과한 뒤 총액만큼 포인트를 검증·차감하며, "
			+ "하나라도 부족하면 어떤 것도 반영되지 않는다(전량 롤백).")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "주문 생성 성공"),
			@ApiResponse(responseCode = "401", description = "미인증", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE))),
			@ApiResponse(responseCode = "400", description = "주문 대상 장바구니 항목이 없음", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":400,"code":"INVALID_REQUEST","message":"주문할 장바구니 항목이 없습니다.","path":"/api/orders"}"""))),
			@ApiResponse(responseCode = "409", description = "재고 부족(INSUFFICIENT_STOCK) 또는 포인트 부족(INSUFFICIENT_POINT), 트랜잭션 롤백", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":409,"code":"INSUFFICIENT_POINT","message":"보유 포인트가 부족합니다. (필요 금액: 1052000, 보유 포인트: 1000000)","path":"/api/orders"}""")))
	})
	@PostMapping
	public ResponseEntity<OrderResponse> create(
			@LoginMember Long memberId,
			@RequestBody(required = false) OrderCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(memberId, request));
	}

	@Operation(summary = "내 주문 목록 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공(페이지네이션)"),
			@ApiResponse(responseCode = "401", description = "미인증")
	})
	@GetMapping
	public ResponseEntity<PageResponse<OrderSummaryResponse>> findAll(
			@LoginMember Long memberId,
			@PageableDefault(size = 20, sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(orderService.findMyOrders(memberId, pageable));
	}

	@Operation(summary = "내 주문 상세 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "미인증"),
			@ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 소유가 아닌 주문", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":404,"code":"ORDER_NOT_FOUND","message":"주문을 찾을 수 없습니다.","path":"/api/orders/999"}""")))
	})
	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> findById(@LoginMember Long memberId, @PathVariable Long id) {
		return ResponseEntity.ok(orderService.findMyOrder(memberId, id));
	}

	@Operation(summary = "주문 취소", description = "주문 전체 단위로만 취소 가능하다(항목 단위 부분 취소 없음). "
			+ "취소 시 각 항목의 도서 재고를 복구하고(삭제된 도서는 제외), 회원 포인트에 주문 총액만큼 환급한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "취소 성공"),
			@ApiResponse(responseCode = "401", description = "미인증"),
			@ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 소유가 아닌 주문"),
			@ApiResponse(responseCode = "409", description = "이미 취소된 주문", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":409,"code":"ORDER_ALREADY_CANCELED","message":"이미 취소된 주문입니다.","path":"/api/orders/1/cancel"}""")))
	})
	@PostMapping("/{id}/cancel")
	public ResponseEntity<OrderResponse> cancel(@LoginMember Long memberId, @PathVariable Long id) {
		return ResponseEntity.ok(orderService.cancelOrder(memberId, id));
	}
}
