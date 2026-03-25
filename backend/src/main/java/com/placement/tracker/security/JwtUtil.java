package com.placement.tracker.security;

import com.placement.tracker.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Utility class that creates and validates JWT tokens.
@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    // Creates JWT with subject=email, role claim, issue time, expiry time, and HMAC signature.
    public String generateToken(String email, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // Validates signature and expiry by trying to parse claims; false means invalid/expired token.
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // Extracts email stored as JWT subject.
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extracts role claim and converts it back to Role enum.
    public Role extractRole(String token) {
        String roleValue = extractAllClaims(token).get("role", String.class);
        return Role.valueOf(roleValue);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSigningKey() {
        // HMAC key must be sufficiently long; use a strong secret value in .env.
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
