package com.skala.shop.controller;

import com.skala.shop.dto.cart.CartItemAddRequest;
import com.skala.shop.dto.cart.CartItemQuantityRequest;
import com.skala.shop.dto.cart.CartItemResponse;
import com.skala.shop.dto.cart.CartResponse;
import com.skala.shop.service.CartService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@PostMapping("/items")
	public ResponseEntity<CartItemResponse> add(@Valid @RequestBody CartItemAddRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(cartService.add(request));
	}

	@GetMapping
	public ResponseEntity<CartResponse> findMyCart() {
		return ResponseEntity.ok(cartService.findMyCart());
	}

	@PatchMapping("/items/{itemId}")
	public ResponseEntity<CartItemResponse> changeQuantity(
			@PathVariable Long itemId,
			@Valid @RequestBody CartItemQuantityRequest request
	) {
		return ResponseEntity.ok(cartService.changeQuantity(itemId, request));
	}

	@DeleteMapping("/items/{itemId}")
	public ResponseEntity<Void> remove(@PathVariable Long itemId) {
		cartService.remove(itemId);
		return ResponseEntity.noContent().build();
	}
}
