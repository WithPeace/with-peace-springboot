package com.example.withpeace.util;

import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OAuth2Util {

    // GOOGLE 용 Data
    @Value("${client.provider.google.authorization-uri: aaa.bbb.ccc}")
    private String GOOGLE_AUTHORIZATION_URL;
    @Value("${client.provider.google.token-uri: aaa.bbb.ccc}")
    private String GOOGLE_TOKEN_URL;
    @Value("${client.provider.google.user-info-uri: aaa.bbb.ccc}")
    private String GOOGLE_USERINFO_URL;

    @Value("${security.oauth2.client.registration.google.client-id: aaa.bbb.ccc}")
    private String GOOGLE_CLIENT_ID;
    @Value("${security.oauth2.client.registration.google.client-secret: aaa.bbb.ccc}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${security.oauth2.client.registration.google.redirect-uri: aaa.bbb.ccc}")
    private String GOOGLE_REDIRECT_URL;

    public String getGoogleUserInformation(final String accessToken) {
        final WebClient webClient = WebClient.builder()
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                })
                .build();

        final String responseJsonBody = webClient.get()
                .uri(GOOGLE_USERINFO_URL)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        clientResponse -> {
                            throw new CommonException(ErrorCode.AUTH_SERVER_USER_INFO_ERROR);
                        })
                .bodyToMono(String.class)
                .flux()
                .toStream()
                .findFirst()
                .orElseThrow(() -> new CommonException(ErrorCode.AUTH_SERVER_USER_INFO_ERROR));

        return JsonParser.parseString(responseJsonBody)
                .getAsJsonObject()
                .get("id")
                .getAsString();
    }
}
