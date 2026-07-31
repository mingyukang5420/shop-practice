package com.skala.skala_shop.domain;

/** MVP는 취소 기능이 없어 주문완료 단일 상태만 존재한다(기능명세서 4.1). API 응답의 "주문완료" 표기는 프레젠테이션 계층에서 매핑한다. */
public enum OrderStatus {
	ORDERED
}
