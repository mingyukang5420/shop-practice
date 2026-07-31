package com.skala.shop.controller;

import com.skala.shop.common.LoginMember;
import com.skala.shop.dto.cart.CartItemAddRequest;
import com.skala.shop.dto.cart.CartItemQuantityRequest;
import com.skala.shop.dto.cart.CartItemResponse;
import com.skala.shop.dto.cart.CartResponse;
import com.skala.shop.exception.ErrorResponse;
import com.skala.shop.service.CartService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

import jakarta.validation.Valid;

@Tag(name = "Cart API", description = "장바구니 담기/조회/수량수정/삭제. 세션 인증이 필요하며, "
		+ "미인증 요청은 401 UNAUTHORIZED를 반환한다.")
@SecurityRequirement(name = "sessionAuth")
@RestController
@RequestMapping("/api/cart")
public class CartController {

	private static final String UNAUTHORIZED_EXAMPLE = """
			{"timestamp":"2026-07-31T09:00:00","status":401,"code":"UNAUTHORIZED","message":"로그인이 필요합니다.","path":"/api/cart"}""";

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@Operation(summary = "장바구니에 도서 담기", description = "동일 도서가 이미 담겨 있으면 신규 행 대신 수량을 합산하고, "
			+ "합산된 총수량이 재고를 초과하면 실패한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "담기 성공"),
			@ApiResponse(responseCode = "401", description = "미인증", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE))),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 도서"),
			@ApiResponse(responseCode = "409", description = "요청(합산) 수량이 재고 초과", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":409,"code":"INSUFFICIENT_STOCK","message":"요청하신 수량이 재고를 초과합니다. (도서: 클린 코드, 요청 수량: 5, 가용 재고: 2)","path":"/api/cart/items"}""")))
	})
	@PostMapping("/items")
	public ResponseEntity<CartItemResponse> add(@LoginMember Long memberId, @Valid @RequestBody CartItemAddRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(cartService.add(memberId, request));
	}

	@Operation(summary = "내 장바구니 조회", description = "price/stock은 스냅샷이 아니라 항상 최신 도서 값으로 반환한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "미인증", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = UNAUTHORIZED_EXAMPLE)))
	})
	@GetMapping
	public ResponseEntity<CartResponse> findMyCart(@LoginMember Long memberId) {
		return ResponseEntity.ok(cartService.findMyCart(memberId));
	}

	@Operation(summary = "장바구니 항목 수량 수정")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(responseCode = "401", description = "미인증"),
			@ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 소유가 아닌 항목"),
			@ApiResponse(responseCode = "409", description = "재고 초과")
	})
	@PatchMapping("/items/{itemId}")
	public ResponseEntity<CartItemResponse> changeQuantity(
			@LoginMember Long memberId,
			@PathVariable Long itemId,
			@Valid @RequestBody CartItemQuantityRequest request
	) {
		return ResponseEntity.ok(cartService.changeQuantity(memberId, itemId, request));
	}

	@Operation(summary = "장바구니 항목 삭제")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공(본문 없음)"),
			@ApiResponse(responseCode = "401", description = "미인증"),
			@ApiResponse(responseCode = "404", description = "존재하지 않거나 본인 소유가 아닌 항목")
	})
	@DeleteMapping("/items/{itemId}")
	public ResponseEntity<Void> remove(@LoginMember Long memberId, @PathVariable Long itemId) {
		cartService.remove(memberId, itemId);
		return ResponseEntity.noContent().build();
	}
}
