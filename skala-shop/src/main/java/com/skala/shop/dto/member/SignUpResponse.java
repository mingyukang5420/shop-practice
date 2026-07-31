package com.skala.shop.dto.member;

public record SignUpResponse(
	Long memberId,
	String loginId,
	String name,
	Integer point
) {
}
