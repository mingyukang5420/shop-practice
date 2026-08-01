package com.skala.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skala.shop.domain.member.MemberRepository;
import com.skala.shop.dto.member.LoginRequest;
import com.skala.shop.dto.member.LoginResponse;
import com.skala.shop.dto.member.MemberResponse;
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

	// 회원가입 시 비밀번호가 평문이 아니라 BCrypt로 암호화되어 저장되는지 검증한다.
	@Test
	void 회원가입_시_비밀번호는_평문으로_저장되지_않는다() {
		SignUpResponse response = authService.signUp(new SignUpRequest("newbie01", "plain1234", "새회원"));

		String savedPassword = memberRepository.findById(response.memberId()).orElseThrow().getPassword();
		assertThat(savedPassword).isNotEqualTo("plain1234");
		assertThat(savedPassword).startsWith("$2a$");
	}

	// 회원가입 시 초기 포인트 1,000,000원이 정상적으로 지급되는지 검증한다.
	@Test
	void 회원가입_시_초기_포인트_1_000_000이_지급된다() {
		SignUpResponse response = authService.signUp(new SignUpRequest("pointuser01", "plain1234", "포인트회원"));

		assertThat(response.point()).isEqualTo(1_000_000);
		assertThat(memberRepository.findById(response.memberId()).orElseThrow().getPoint()).isEqualTo(1_000_000);
	}

	// 이미 존재하는 로그인 id로 다시 가입하려 하면 DUPLICATE_LOGIN_ID 예외가 발생하는지 검증한다.
	@Test
	void 중복된_아이디로_가입하면_DUPLICATE_LOGIN_ID를_던진다() {
		authService.signUp(new SignUpRequest("dup01", "plain1234", "회원1"));

		assertThatThrownBy(() -> authService.signUp(new SignUpRequest("dup01", "other1234", "회원2")))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID));
	}

	// 로그인에 성공하면 세션에 회원 id가 저장되는지 검증한다.
	@Test
	void 로그인에_성공하면_세션에_회원id가_저장된다() {
		authService.signUp(new SignUpRequest("login01", "plain1234", "로그인테스트"));
		MockHttpSession session = new MockHttpSession();

		LoginResponse response = authService.login(new LoginRequest("login01", "plain1234"), session);

		assertThat(session.getAttribute(AuthService.SESSION_MEMBER_ID)).isEqualTo(response.memberId());
	}

	// 존재하지 않는 로그인 id로 로그인하면 INVALID_CREDENTIALS 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_아이디로_로그인하면_INVALID_CREDENTIALS를_던진다() {
		assertThatThrownBy(() -> authService.login(new LoginRequest("no-such-id", "plain1234"), new MockHttpSession()))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
	}

	// 비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외가 발생하는지 검증한다.
	@Test
	void 비밀번호가_틀리면_INVALID_CREDENTIALS를_던진다() {
		authService.signUp(new SignUpRequest("wrongpw01", "plain1234", "회원"));

		assertThatThrownBy(() -> authService.login(new LoginRequest("wrongpw01", "wrong-password"), new MockHttpSession()))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
	}

	// 내 정보 조회 시 가입 시점이 아닌 현재 시점의 실제 포인트가 반환되는지 검증한다.
	@Test
	void 내_정보_조회는_현재_시점의_실제_포인트를_반환한다() {
		SignUpResponse signedUp = authService.signUp(new SignUpRequest("meinfo01", "plain1234", "정보조회회원"));

		MemberResponse response = authService.getMyInfo(signedUp.memberId());

		assertThat(response.memberId()).isEqualTo(signedUp.memberId());
		assertThat(response.loginId()).isEqualTo("meinfo01");
		assertThat(response.point()).isEqualTo(1_000_000);
	}

	// 존재하지 않는 회원 id로 내 정보를 조회하면 UNAUTHORIZED 예외가 발생하는지 검증한다.
	@Test
	void 존재하지_않는_회원id로_내_정보_조회시_UNAUTHORIZED를_던진다() {
		assertThatThrownBy(() -> authService.getMyInfo(9999L))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
	}
}
