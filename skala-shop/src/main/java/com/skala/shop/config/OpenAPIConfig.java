package com.skala.shop.config;

import com.skala.shop.common.LoginMember;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
		info = @Info(
				title = "SKALA SHOP API",
				version = "1.0.0",
				description = "도서 쇼핑몰 백엔드 REST API. Cart/Order API는 세션 인증(로그인 후 발급되는 "
						+ "JSESSIONID 쿠키)이 필요하며, 이 문서에서는 각 엔드포인트가 인증을 요구하는지 설명에 표시했다."
		)
)
@SecurityScheme(
		name = "sessionAuth",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.COOKIE,
		paramName = "JSESSIONID",
		description = "POST /api/members/login 성공 시 발급되는 세션 쿠키(JWT 아님)."
)
@Configuration
public class OpenAPIConfig {

	static {
		// @LoginMember는 세션에서 자동 주입되는 값이라 클라이언트가 채울 값이 아니다.
		// 등록해두지 않으면 springdoc이 이를 일반 파라미터로 오인해 memberId를 필수 쿼리 파라미터로 노출한다.
		SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginMember.class);
	}

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI();
	}
}
