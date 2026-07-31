package com.skala.shop.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(
			BusinessException exception,
			HttpServletRequest request
	) {
		ErrorCode errorCode = exception.getErrorCode();
		log.warn("BusinessException: {} {}", errorCode.getCode(), request.getRequestURI());
		return ResponseEntity.status(errorCode.getStatus())
				.body(createResponse(errorCode, exception.getMessage(), request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.sorted()
				.collect(Collectors.joining(", "));
		if (message.isBlank()) {
			message = ErrorCode.VALIDATION_ERROR.getMessage();
		}
		return ResponseEntity.badRequest()
				.body(createResponse(ErrorCode.VALIDATION_ERROR, message, request.getRequestURI()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableRequest(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return ResponseEntity.badRequest()
				.body(createResponse(
						ErrorCode.VALIDATION_ERROR,
						"JSON 형식과 필드 값을 확인해 주세요.",
						request.getRequestURI()
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(
			Exception exception,
			HttpServletRequest request
	) {
		log.error("Unexpected exception at {}", request.getRequestURI(), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(createResponse(
						ErrorCode.INTERNAL_ERROR,
						ErrorCode.INTERNAL_ERROR.getMessage(),
						request.getRequestURI()
				));
	}

	private ErrorResponse createResponse(ErrorCode errorCode, String message, String path) {
		return new ErrorResponse(
				LocalDateTime.now(),
				errorCode.getStatus().value(),
				errorCode.getCode(),
				message,
				path
		);
	}
}
