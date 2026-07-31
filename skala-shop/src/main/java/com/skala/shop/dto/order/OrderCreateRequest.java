package com.skala.shop.dto.order;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** 미지정(요청 본문 없음) 시 장바구니 전체를, 지정 시 해당 장바구니 항목만 주문으로 전환한다(API명세서 4절). */
public record OrderCreateRequest(
	@Schema(description = "부분 주문 시 대상 장바구니 항목 id 목록. 생략하면 장바구니 전체를 주문한다.", example = "[1, 2]")
	List<Long> cartItemIds
) {
}
