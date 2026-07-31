package com.skala.shop.exception;

public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	/** 도서명/요청수량/가용재고를 메시지에 포함한 INSUFFICIENT_STOCK 예외를 만든다(API명세서 7.1). */
	public static BusinessException insufficientStock(String bookTitle, int requestedQuantity, int availableStock) {
		String message = "요청하신 수량이 재고를 초과합니다. (도서: %s, 요청 수량: %d, 가용 재고: %d)"
				.formatted(bookTitle, requestedQuantity, availableStock);
		return new BusinessException(ErrorCode.INSUFFICIENT_STOCK, message);
	}

	/** 필요 금액/보유 포인트를 메시지에 포함한 INSUFFICIENT_POINT 예외를 만든다. */
	public static BusinessException insufficientPoint(int requiredPoint, int availablePoint) {
		String message = "보유 포인트가 부족합니다. (필요 금액: %d, 보유 포인트: %d)"
				.formatted(requiredPoint, availablePoint);
		return new BusinessException(ErrorCode.INSUFFICIENT_POINT, message);
	}
}
