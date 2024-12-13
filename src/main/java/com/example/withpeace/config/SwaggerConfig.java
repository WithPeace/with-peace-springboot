package com.example.withpeace.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        // 소셜 로그인용 SecurityScheme
        SecurityScheme socialAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Social Client ID - 소셜 로그인(Google/Apple) 요청 시 사용할 클라이언트 ID를 입력하세요");

        // 서비스 액세스 토큰용 SecurityScheme
        SecurityScheme accessTokenScheme  = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Access Token - 소셜 로그인 후 받은 액세스 토큰을 입력하세요");

        // 기본적으로 Access Token 사용하도록 설정
        SecurityRequirement defaultRequirement = new SecurityRequirement()
                .addList("Access Token");

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .components(new Components().addSecuritySchemes("Bearer Token", securityScheme))
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("Withpeace Swagger")
                .description("Provides documentation for the Withpeace API.")
                .version("1.0.0");
    }
}
