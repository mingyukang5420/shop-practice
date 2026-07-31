package com.skala.shop.dto.member;

import com.skala.shop.domain.member.Member;

import io.swagger.v3.oas.annotations.media.Schema;

/** GET /api/members/me 응답 — 주문/취소로 변동된 현재 시점의 실제 포인트를 담는다(스냅샷 아님). */
public record MemberResponse(
	@Schema(example = "1") Long memberId,
	@Schema(example = "dummy") String loginId,
	@Schema(example = "더미 회원") String name,
	@Schema(description = "현재 보유 포인트", example = "940000") Integer point
) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getLoginId(), member.getName(), member.getPoint());
	}
}
