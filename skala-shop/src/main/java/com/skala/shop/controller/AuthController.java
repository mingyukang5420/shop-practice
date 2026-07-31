package com.skala.shop.controller;

import com.skala.shop.common.LoginMember;
import com.skala.shop.dto.member.LoginRequest;
import com.skala.shop.dto.member.LoginResponse;
import com.skala.shop.dto.member.MemberResponse;
import com.skala.shop.dto.member.SignUpRequest;
import com.skala.shop.dto.member.SignUpResponse;
import com.skala.shop.exception.ErrorResponse;
import com.skala.shop.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Auth(Member) API", description = "회원가입/로그인/로그아웃. 로그인 성공 시 세션 쿠키(JSESSIONID)가 발급되며, "
		+ "이후 Cart/Order API 요청에는 이 쿠키가 필요하다(JWT 아님).")
@RestController
@RequestMapping("/api/members")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@Operation(summary = "회원가입", description = "아이디/비밀번호(BCrypt로 암호화되어 저장)/이름으로 가입한다. "
			+ "가입 즉시 초기 포인트 1,000,000이 지급된다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "가입 성공"),
			@ApiResponse(responseCode = "400", description = "필수값 누락/형식 오류", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":400,"code":"VALIDATION_ERROR","message":"loginId: 아이디는 필수입니다.","path":"/api/members"}"""))),
			@ApiResponse(responseCode = "409", description = "이미 사용 중인 아이디", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":409,"code":"DUPLICATE_LOGIN_ID","message":"이미 사용 중인 아이디입니다.","path":"/api/members"}""")))
	})
	@PostMapping
	public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
	}

	@Operation(summary = "로그인", description = "성공 시 세션 쿠키(JSESSIONID)를 응답 헤더로 발급한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공, Set-Cookie: JSESSIONID=... 헤더 포함"),
			@ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":401,"code":"INVALID_CREDENTIALS","message":"아이디 또는 비밀번호가 올바르지 않습니다.","path":"/api/members/login"}""")))
	})
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
		return ResponseEntity.ok(authService.login(request, servletRequest.getSession(true)));
	}

	@Operation(summary = "로그아웃", description = "현재 세션을 무효화한다. 세션이 없어도 오류 없이 204를 반환한다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "로그아웃 성공(본문 없음)")
	})
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
		authService.logout(servletRequest.getSession(false));
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "내 정보 조회", description = "주문/취소로 변동된 현재 시점의 실제 보유 포인트를 확인한다(스냅샷 아님).")
	@SecurityRequirement(name = "sessionAuth")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "미인증", content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = """
							{"timestamp":"2026-07-31T09:00:00","status":401,"code":"UNAUTHORIZED","message":"로그인이 필요합니다.","path":"/api/members/me"}""")))
	})
	@GetMapping("/me")
	public ResponseEntity<MemberResponse> me(@LoginMember Long memberId) {
		return ResponseEntity.ok(authService.getMyInfo(memberId));
	}
}
