package com.hrms.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long jwtExpirationMs;
    private final Environment env;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expirationMs}") long jwtExpirationMs, Environment env) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        this.env = env;
    }

    @PostConstruct
    public void validateSecret() {
        boolean isProd = false;
        for (String profile : env.getActiveProfiles()) {
            if ("prod".equals(profile)) {
                isProd = true;
                break;
            }
        }
        
        if (isProd) {
            String secret = new String(key.getEncoded());
            if (secret.equals("averylongsecretkeywhichisatleast256bitslongforhs256signature")) {
                throw new IllegalStateException("FATAL: Weak or default JWT secret detected in 'prod' profile. Refusing to start.");
            }
        }
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
