package com.example.withpeace.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.util.Arrays;

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

        Server prodServer = new Server().url("https://cheongha.site"); // 운영 서버 URL
        Server localServer = new Server().url("http://localhost:8080"); // 로컬 서버 URL

        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .addSecuritySchemes("Social Auth", socialAuthScheme)
                        .addSecuritySchemes("Access Token", accessTokenScheme))
                .addSecurityItem(defaultRequirement) // 전역 설정으로 Access Token 사용
                .servers(Arrays.asList(prodServer, localServer))

                .tags(Arrays.asList(
                        new Tag().name("Auth").description("인증 및 회원 관리 API"),
                        new Tag().name("User").description("사용자 정보 관리 API"),
                        new Tag().name("Post").description("게시글 관련 API"),
                        new Tag().name("App").description("안드로이드 앱 버전 관리 API"),
                        new Tag().name("Policy").description("정책 관련 API"),
                        new Tag().name("BalanceGame").description("밸런스게임 관련 API")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("CheongHa API Documentation")
                .description("청하 API 문서입니다. \n\n" +
                        "인증 프로세스\n" +
                        "1. Authorize 버튼을 클릭하여 Social Auth에 클라이언트 ID 입력\n" +
                        "2. 소셜 로그인(Google/Apple) API 호출하여 액세스 토큰 발급\n" +
                        "3. 발급받은 액세스 토큰을 Access Token에 입력\n" +
                        "4. 이후 모든 API 요청 가능")
                .version("2.0.0");
    }
}
