package com.skala.shop.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
	@Schema(example = "1") Long memberId,
	@Schema(example = "dummy") String loginId,
	@Schema(example = "더미 회원") String name
) {
}
