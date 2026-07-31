package com.skala.shop.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResponse(
	@Schema(example = "1") Long memberId,
	@Schema(example = "newbie01") String loginId,
	@Schema(example = "신규회원") String name,
	@Schema(description = "가입 시 지급되는 초기 포인트", example = "1000000") Integer point
) {
}
