package com.capitan.chatapp.security;

import java.security.Key;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class SecurityConstants {
    public static final Key JWT_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    public static final long JWT_EXPIRATION = 3 * 60 * 60 * 1000;

}
