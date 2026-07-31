package com.skala.shop.service;

import com.skala.shop.domain.member.Member;
import com.skala.shop.domain.member.MemberRepository;
import com.skala.shop.dto.member.LoginRequest;
import com.skala.shop.dto.member.LoginResponse;
import com.skala.shop.dto.member.SignUpRequest;
import com.skala.shop.dto.member.SignUpResponse;
import com.skala.shop.exception.BusinessException;
import com.skala.shop.exception.ErrorCode;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

@Service
@Transactional(readOnly = true)
public class AuthService {

	/** 로그인한 회원 id를 세션에 저장할 때 쓰는 attribute 키. JWT 대신 세션(쿠키) 기반으로 로그인 상태를 유지한다. */
	public static final String SESSION_MEMBER_ID = "MEMBER_ID";

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		if (memberRepository.existsByLoginId(request.loginId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
		}
		Member member = new Member(request.loginId(), passwordEncoder.encode(request.password()), request.name());
		memberRepository.save(member);
		return new SignUpResponse(member.getId(), member.getLoginId(), member.getName());
	}

	/** 인증 성공 시 세션에 회원 id를 저장한다. 이후 요청은 브라우저가 자동으로 보내는 세션 쿠키로 식별된다. */
	public LoginResponse login(LoginRequest request, HttpSession session) {
		Member member = memberRepository.findByLoginId(request.loginId())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}
		session.setAttribute(SESSION_MEMBER_ID, member.getId());
		return new LoginResponse(member.getId(), member.getLoginId(), member.getName());
	}

	public void logout(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
	}
}
