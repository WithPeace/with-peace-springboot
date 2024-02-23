package com.example.withpeace.util;

import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.CLIENT_ID;

;

@Component
@RequiredArgsConstructor
public class OAuth2Util {

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();

    public String getGoogleUserInformation(final String accessToken) throws CommonException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), accessToken);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getSubject().toString();
        } else {
            throw new CommonException(ErrorCode.AUTH_SERVER_USER_INFO_ERROR);
        }


    }
}
