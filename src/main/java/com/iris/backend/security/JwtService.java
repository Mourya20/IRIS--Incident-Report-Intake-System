package com.iris.backend.security;

import com.iris.backend.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-minutes}") long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(resolveSecret(secret));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(java.util.stream.Collectors.toSet());
        return Jwts.builder()
                .subject(user.getUsername())
                .claims(Map.of("roles", roles, "uid", user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    private byte[] resolveSecret(String secret) {
        String normalized = secret.trim();
        if (normalized.length() >= 32) {
            return normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            byte[] decoded = Decoders.BASE64.decode(normalized);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("JWT secret must be at least 32 characters or base64 equivalent");
    }
}
