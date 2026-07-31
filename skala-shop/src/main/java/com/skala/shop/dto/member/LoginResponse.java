package com.skala.shop.dto.member;

public record LoginResponse(
	Long memberId,
	String loginId,
	String name
) {
}
