package com.skala.shop.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

	@NotBlank(message = "아이디는 필수입니다.")
	@Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
	String loginId,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
	String password,

	@NotBlank(message = "이름은 필수입니다.")
	String name
) {
}
