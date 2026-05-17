package com.cursoIntegrador.novaBackend.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET_KEY = "llave_De_Prueba_Momentanea_12345_ExtendedForSecurityRequirements";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateToken_Success() {
        String token = jwtUtil.generateToken("usuario@prueba.com");

        assertNotNull(token);
        assertTrue(token.contains("."));
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length);
    }

    @Test
    void testGenerateToken_MultipleCalls_DifferentTokens() {
        String token1 = jwtUtil.generateToken("usuario@prueba.com");
        String token2 = jwtUtil.generateToken("usuario@prueba.com");

        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(token1.contains("."));
        assertTrue(token2.contains("."));
    }

    @Test
    void testGenerateToken_WithSpecialCharactersEmail() {
        String emailEspecial = "usuario+tag@prueba.com";
        String token = jwtUtil.generateToken(emailEspecial);

        assertNotNull(token);
        assertEquals(emailEspecial, jwtUtil.validateAndGetUser(token));
    }

    @Test
    void testGenerateToken_WithLongEmail() {
        String emailLargo = "very.long.email.address.with.many.characters@test.example.com";
        String token = jwtUtil.generateToken(emailLargo);

        assertNotNull(token);
        assertEquals(emailLargo, jwtUtil.validateAndGetUser(token));
    }

    @Test
    void testValidateAndGetUser_Success() {
        String usuario = "usuario@prueba.com";
        String token = jwtUtil.generateToken(usuario);

        String username = jwtUtil.validateAndGetUser(token);

        assertEquals(usuario, username);
    }

    @Test
    void testValidateAndGetUser_MultipleUsers() {
        String usuario1 = "usuario1@prueba.com";
        String usuario2 = "usuario2@prueba.com";

        String token1 = jwtUtil.generateToken(usuario1);
        String token2 = jwtUtil.generateToken(usuario2);

        assertEquals(usuario1, jwtUtil.validateAndGetUser(token1));
        assertEquals(usuario2, jwtUtil.validateAndGetUser(token2));
    }

    @Test
    void testValidateAndGetUser_EmptyToken() {
        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetUser(""));
    }

    @Test
    void testValidateAndGetUser_TokenMutated() {
        String token = jwtUtil.generateToken("usuario@prueba.com");
        String mutated = token.substring(0, token.length() - 1) + "X";

        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetUser(mutated));
    }

    @Test
    void testValidateAndGetUser_InvalidTokenWithWrongSecret() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("different_secret_key_123456789_ExtendedForSecurityRequirements".getBytes(StandardCharsets.UTF_8));
        String invalidToken = Jwts.builder().subject("usuario@prueba.com").signWith(wrongKey).compact();

        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetUser(invalidToken));
    }

    @Test
    void testValidateAndGetUser_InvalidFormat_TwoParts() {
        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetUser("only-two-parts.here"));
    }

    @Test
    void testValidateAndGetUser_InvalidFormat_FourParts() {
        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetUser("a.b.c.d"));
    }

    @Test
    void testValidateAndGetUser_ExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("usuario@prueba.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(4000)))
                .expiration(Date.from(Instant.now().minusSeconds(1000)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(RuntimeException.class, () -> jwtUtil.validateAndGetUser(expiredToken));
    }

    @Test
    void testValidateTokenAndGetClaims_Success() {
        String usuario = "usuario@prueba.com";
        String token = jwtUtil.generateToken(usuario);

        Claims claims = jwtUtil.validateTokenAndGetClaims(token);

        assertNotNull(claims);
        assertEquals(usuario, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testValidateTokenAndGetClaims_ClaimsContent() {
        String usuario = "test@example.com";
        String token = jwtUtil.generateToken(usuario);

        Claims claims = jwtUtil.validateTokenAndGetClaims(token);

        assertEquals(usuario, claims.getSubject());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void testValidateTokenAndGetClaims_ExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("usuario@prueba.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(4000)))
                .expiration(Date.from(Instant.now().minusSeconds(1000)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(RuntimeException.class, () -> jwtUtil.validateTokenAndGetClaims(expiredToken));
    }

    @Test
    void testValidateTokenAndGetClaims_InvalidSignature() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong_secret_key_1234567890_ExtendedForSecurityRequirements".getBytes(StandardCharsets.UTF_8));
        String invalidToken = Jwts.builder().subject("usuario@prueba.com").signWith(wrongKey).compact();

        assertThrows(RuntimeException.class, () -> jwtUtil.validateTokenAndGetClaims(invalidToken));
    }

    @Test
    void testValidateTokenAndGetClaims_EmptyToken() {
        assertThrows(RuntimeException.class, () -> jwtUtil.validateTokenAndGetClaims(""));
    }

    @Test
    void testValidateTokenAndGetClaims_MalformedToken() {
        assertThrows(RuntimeException.class, () -> jwtUtil.validateTokenAndGetClaims("malformed.token"));
        assertThrows(RuntimeException.class, () -> jwtUtil.validateTokenAndGetClaims("just.one"));
        assertThrows(RuntimeException.class, () -> jwtUtil.validateTokenAndGetClaims("a.b.c.d.e"));
    }
}

