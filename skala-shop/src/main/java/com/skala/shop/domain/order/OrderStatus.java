package com.skala.shop.domain.order;

/** MVP는 취소 기능이 없어 주문완료 단일 상태만 존재한다(기능명세서 4.1). */
public enum OrderStatus {
	ORDERED("주문완료");

	private final String displayName;

	OrderStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
