package com.skala.shop.dto.order;

import java.util.List;

/** 미지정(요청 본문 없음) 시 장바구니 전체를, 지정 시 해당 장바구니 항목만 주문으로 전환한다(API명세서 4절). */
public record OrderCreateRequest(
	List<Long> cartItemIds
) {
}
