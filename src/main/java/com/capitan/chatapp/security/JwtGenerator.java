package com.capitan.chatapp.security;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtGenerator {

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPIRATION);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SecurityConstants.JWT_SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    public Cookie generateCookie(Authentication authentication) {
        String token = generateToken(authentication);

        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setMaxAge((int) SecurityConstants.JWT_EXPIRATION / 1000);
        cookie.setDomain("localhost");
        cookie.setPath("/");

        return cookie;
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SecurityConstants.JWT_SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SecurityConstants.JWT_SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("JWT is expired or incorrect!");
        }
    }

    private String getJWTFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getUserNameFromJWTCookies(HttpServletRequest request) {
        return getUsernameFromJwt(getJWTFromCookies(request));
    }

}
