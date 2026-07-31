package com.skala.shop.controller;

import com.skala.shop.dto.member.LoginRequest;
import com.skala.shop.dto.member.LoginResponse;
import com.skala.shop.dto.member.SignUpRequest;
import com.skala.shop.dto.member.SignUpResponse;
import com.skala.shop.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping
	public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
		return ResponseEntity.ok(authService.login(request, servletRequest.getSession(true)));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
		authService.logout(servletRequest.getSession(false));
		return ResponseEntity.noContent().build();
	}
}
