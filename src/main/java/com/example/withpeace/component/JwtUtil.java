package com.example.withpeace.component;


import com.example.withpeace.constant.Constant;
import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.type.ERole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil implements InitializingBean {
    private static final Integer ACCESS_EXPIRED_MS = 2 * 60 * 60 * 1000;
    private static final Integer REFRESH_EXPIRED_MS = 7 * 24 * 60 * 60 * 1000;

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenDto generateTokens(Long id, ERole eRole) {
        return new JwtTokenDto(generateAccessToken(id, eRole, ACCESS_EXPIRED_MS),
                generateToken(null, REFRESH_EXPIRED_MS));
    }

    public Claims validateToken(final String token) throws JwtException {
        final JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public String generateToken(Claims claims, final Integer expirationPeriod) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationPeriod))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateAccessToken(final Long id, final ERole role, final Integer expirationPeriod) {
        final Claims claims = Jwts.claims();
        claims.put(Constant.USER_ID_CLAIM_NAME, id.toString());
        claims.put(Constant.USER_ROLE_CLAIM_NAME, role.toString());

        return generateToken(claims, expirationPeriod);
    }

    public int getAccessTokenExpriration() {
        return ACCESS_EXPIRED_MS;
    }

    public int getWebRefreshTokenExpirationSecond() {
        return (REFRESH_EXPIRED_MS / 1000);
    }

}