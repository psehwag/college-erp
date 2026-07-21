package com.erp.auth.security;

import com.erp.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("referenceId",
                user.getReferenceId() != null ? user.getReferenceId().toString() : null);
        claims.put("mustChangePassword", user.getMustChangePassword());
        return buildToken(claims, user.getUsername(), expiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenId", UUID.randomUUID().toString());
        return buildToken(claims, user.getUsername(), refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, Long exp) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpiration() {
        return expiration;
    }
}
