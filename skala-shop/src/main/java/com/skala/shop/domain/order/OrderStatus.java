package com.skala.shop.domain.order;

/** 주문완료/주문취소 두 상태를 가진다. */
public enum OrderStatus {
	ORDERED("주문완료"),
	CANCELED("주문취소");

	private final String displayName;

	OrderStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
