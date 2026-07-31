package com.skala.shop.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

	@NotBlank(message = "아이디는 필수입니다.")
	@Schema(example = "dummy")
	String loginId,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Schema(example = "dummy1234")
	String password
) {
}
