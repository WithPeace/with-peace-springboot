package com.example.withpeace.util;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.CLIENT_ID;

;

@Component
public class OAuth2Util {

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();

    private GoogleIdTokenVerifier verifier;

    public OAuth2Util() {
        verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
    }

    public String getGoogleUserIdToken(final String accessToken) throws CommonException, IOException {
        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), accessToken);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getSubject().toString();
        } else {
            throw new CommonException(ErrorCode.AUTH_SERVER_USER_INFO_ERROR);
        }

    }

    public String getGoogleUserEmail(final String accessToken) throws CommonException, IOException {
        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), accessToken);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getEmail();
        } else {
            throw new CommonException(ErrorCode.AUTH_SERVER_USER_INFO_ERROR);
        }
    }

    public String getAppleUserIdToken(String accessToken) throws IOException, JwkException {
        // JWKS URL from Apple
        JwkProvider provider = new UrlJwkProvider(new URL("https://appleid.apple.com/auth/keys"));

        // Decode the token to get the key ID
        DecodedJWT decodedJWT = JWT.decode(accessToken);

        // Fetch the JWK and convert to RSA Public Key
        RSAPublicKey publicKey = (RSAPublicKey) provider.get(decodedJWT.getKeyId()).getPublicKey();

        // Verify the token
        DecodedJWT jwt = JWT.require(Algorithm.RSA256(publicKey, null)).build().verify(accessToken);

        // Return the user ID from the token payload
        return jwt.getClaim("sub").asString();
    }
}
