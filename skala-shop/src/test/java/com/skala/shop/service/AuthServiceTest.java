package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.domain.member.MemberRepository;
import com.skala.shop.dto.member.LoginRequest;
import com.skala.shop.dto.member.LoginResponse;
import com.skala.shop.dto.member.SignUpRequest;
import com.skala.shop.dto.member.SignUpResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	void 회원가입_시_비밀번호는_평문으로_저장되지_않는다() {
		SignUpResponse response = authService.signUp(new SignUpRequest("newbie01", "plain1234", "새회원"));

		String savedPassword = memberRepository.findById(response.memberId()).orElseThrow().getPassword();
		assertThat(savedPassword).isNotEqualTo("plain1234");
		assertThat(savedPassword).startsWith("$2a$");
	}

	@Test
	void 중복된_아이디로_가입하면_DUPLICATE_LOGIN_ID를_던진다() {
		authService.signUp(new SignUpRequest("dup01", "plain1234", "회원1"));

		assertThatThrownBy(() -> authService.signUp(new SignUpRequest("dup01", "other1234", "회원2")))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID));
	}

	@Test
	void 로그인에_성공하면_세션에_회원id가_저장된다() {
		authService.signUp(new SignUpRequest("login01", "plain1234", "로그인테스트"));
		MockHttpSession session = new MockHttpSession();

		LoginResponse response = authService.login(new LoginRequest("login01", "plain1234"), session);

		assertThat(session.getAttribute(AuthService.SESSION_MEMBER_ID)).isEqualTo(response.memberId());
	}

	@Test
	void 존재하지_않는_아이디로_로그인하면_INVALID_CREDENTIALS를_던진다() {
		assertThatThrownBy(() -> authService.login(new LoginRequest("no-such-id", "plain1234"), new MockHttpSession()))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
	}

	@Test
	void 비밀번호가_틀리면_INVALID_CREDENTIALS를_던진다() {
		authService.signUp(new SignUpRequest("wrongpw01", "plain1234", "회원"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("wrongpw01", "wrong-password"), new MockHttpSession()))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
	}
}
