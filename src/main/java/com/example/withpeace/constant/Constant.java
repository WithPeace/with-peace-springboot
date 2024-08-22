package com.example.withpeace.constant;

import java.util.List;

public class Constant {
    public static final String USER_ID_CLAIM_NAME = "uid";
    public static final String USER_ROLE_CLAIM_NAME = "rol";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "accessToken";
    public static final String REAUTHORIZATION = "refreshToken";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final List<String> NO_NEED_AUTH_URLS = List.of(
            "/api/v1/auth/google",
            "/api/v1/auth/refresh",
            "/api/v1/users/profile/nickname/check",
            "/api/v1/users/recovery",
            "/api/v1/app/check/android",
            "/v3/api-docs",
            "/swagger-ui/index.html",
            "/swagger-ui/swagger-ui.css",
            "/swagger-ui/favicon-32x32.png",
            "/v3/api-docs/swagger-config",
            "/swagger-ui/swagger-ui-bundle.js",
            "/swagger-ui/swagger-initializer.js",
            "/swagger-ui/swagger-ui-standalone-preset.js",
            "/swagger-ui/index.css",
            "/favicon.ico",
            "/actuator/prometheus"
    );
}
