package com.cursoIntegrador.novaBackend.Security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String LLAVE = "llave_De_Prueba_Momentanea_12345";
    private final SecretKey secretKey;

    public JwtUtil() {
        this.secretKey = Keys.hmacShaKeyFor(LLAVE.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expires = Date.from(now.plusSeconds(3600));

        String token = Jwts.builder().subject(username).issuedAt(issuedAt).expiration(expires).signWith(secretKey)
                .compact();

        return token;
    }

    public String validateAndGetUser(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        return claims.getSubject();
    }

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Token inválido o expirado", e);
        }
    }
}
