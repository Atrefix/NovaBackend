package com.cursoIntegrador.novaBackend.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountLoginDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Security.JwtUtil;
import com.cursoIntegrador.novaBackend.Service.DAO.AccountService;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AccountService accountService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Cuenta mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new Cuenta(
                1,
                "usuario@prueba.com",
                "contraseñaEncriptada",
                "USER",
                "ACTIVO",
                LocalDateTime.now(),
                "1234567890");
    }

    @Test
    void testLogin_Success() {
        Authentication mockAuth = Mockito.mock(Authentication.class);
        org.springframework.security.core.userdetails.UserDetails mockUserDetails = Mockito.mock(
                org.springframework.security.core.userdetails.UserDetails.class);
        
        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);
        when(mockUserDetails.getUsername()).thenReturn("usuario@prueba.com");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);
        when(jwtUtil.generateToken("usuario@prueba.com")).thenReturn("fakeToken");

        Map<String, Object> result = authService.login("usuario@prueba.com", "contraseña123");

        AccountLoginDTO loginData = (AccountLoginDTO) result.get("loginData");
        assertEquals("usuario@prueba.com", loginData.getEmail());
        assertEquals("fakeToken", loginData.getToken());
        verify(jwtUtil).generateToken("usuario@prueba.com");
    }

    @Test
    void testLogin_Failed_InvalidCredentials() {
        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> authService.login("usuario@prueba.com", "contraseñaIncorrecta"));

        assertEquals("Credenciales inválidas", thrown.getMessage());
    }

    @Test
    void testLogin_ReturnsCorrectTokenAndEmail() {
        Authentication mockAuth = Mockito.mock(Authentication.class);
        org.springframework.security.core.userdetails.UserDetails mockUserDetails = Mockito.mock(
                org.springframework.security.core.userdetails.UserDetails.class);
        
        when(authenticationManager.authenticate(Mockito.any())).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);
        when(mockUserDetails.getUsername()).thenReturn("usuario@prueba.com");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);
        when(jwtUtil.generateToken("usuario@prueba.com")).thenReturn("token123");

        Map<String, Object> result = authService.login("usuario@prueba.com", "pass");

        assertTrue(result.containsKey("loginData"));
        verify(accountService).findByEmail("usuario@prueba.com");
    }

    @Test
    void testRegister_Success() {
        when(accountService.findByEmail("newuser@prueba.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("encriptada");

        authService.register("newuser@prueba.com", "password123");

        verify(passwordEncoder).encode("password123");
        verify(accountService).save(any(Cuenta.class));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> authService.register("usuario@prueba.com", "password123"));

        assertEquals("El usuario ya está registrado", thrown.getMessage());
        verify(accountService, never()).save(any());
    }

    @Test
    void testRegister_EncryptsPassword() {
        when(accountService.findByEmail("newuser@prueba.com")).thenReturn(null);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");

        authService.register("newuser@prueba.com", "plainPassword");

        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void testUserExists_True() {
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        boolean exists = authService.userExists("usuario@prueba.com");

        assertTrue(exists);
        verify(accountService).findByEmail("usuario@prueba.com");
    }

    @Test
    void testUserExists_False() {
        when(accountService.findByEmail("noexiste@prueba.com")).thenReturn(null);

        boolean exists = authService.userExists("noexiste@prueba.com");

        assertFalse(exists);
    }

    @Test
    void testExtractUsername_Success() {
        when(jwtUtil.validateAndGetUser("validToken")).thenReturn("usuario@prueba.com");

        String username = authService.extractUsername("validToken");

        assertEquals("usuario@prueba.com", username);
        verify(jwtUtil).validateAndGetUser("validToken");
    }

    @Test
    void testExtractUsername_InvalidToken() {
        when(jwtUtil.validateAndGetUser("invalidToken")).thenThrow(new RuntimeException("Token inválido"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> authService.extractUsername("invalidToken"));

        assertEquals("Token inválido", thrown.getMessage());
    }

    @Test
    void testInvalidateToken_Success() {
        authService.invalidateToken("tokenToInvalidate");
        
        assertFalse(authService.isTokenValid("tokenToInvalidate"));
    }

    @Test
    void testInvalidateToken_MultipleTokens() {
        authService.invalidateToken("token1");
        authService.invalidateToken("token2");
        
        assertFalse(authService.isTokenValid("token1"));
        assertFalse(authService.isTokenValid("token2"));
    }

    @Test
    void testIsTokenValid_ValidToken() {
        when(jwtUtil.validateTokenAndGetClaims("validToken")).thenReturn(null);

        boolean valid = authService.isTokenValid("validToken");

        assertTrue(valid);
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        when(jwtUtil.validateTokenAndGetClaims("invalidToken")).thenThrow(new RuntimeException("Invalid"));

        boolean valid = authService.isTokenValid("invalidToken");

        assertFalse(valid);
    }

    @Test
    void testIsTokenValid_InvalidatedToken() {
        authService.invalidateToken("token123");

        boolean valid = authService.isTokenValid("token123");

        assertFalse(valid);
    }

    @Test
    void testIsTokenValid_InvalidatedTokenChecksBefore() {
        authService.invalidateToken("blacklistedToken");

        boolean valid = authService.isTokenValid("blacklistedToken");

        assertFalse(valid);
        verify(jwtUtil, never()).validateTokenAndGetClaims("blacklistedToken");
    }

    @Test
    void testActualizarPassword_Success() {
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");

        authService.actualizarPassword("usuario@prueba.com", "newPassword");

        verify(passwordEncoder).encode("newPassword");
        verify(accountService).updatePassword("usuario@prueba.com", "hashedNewPassword");
    }

    @Test
    void testActualizarPassword_UserNotFound() {
        when(accountService.findByEmail("noexiste@prueba.com")).thenReturn(null);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> authService.actualizarPassword("noexiste@prueba.com", "newPassword"));

        assertEquals("Usuario no encontrado", thrown.getMessage());
        verify(accountService, never()).updatePassword(any(), any());
    }

    @Test
    void testActualizarPassword_EncryptsNewPassword() {
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);
        when(passwordEncoder.encode("plainNewPass")).thenReturn("encrypted");

        authService.actualizarPassword("usuario@prueba.com", "plainNewPass");

        verify(passwordEncoder).encode("plainNewPass");
        verify(accountService).updatePassword("usuario@prueba.com", "encrypted");
    }

    @Test
    void testUserHasRole_True() {
        mockUser.setRol("ADMIN");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        boolean hasRole = authService.userHasRole("usuario@prueba.com", "ADMIN");

        assertTrue(hasRole);
    }

    @Test
    void testUserHasRole_False() {
        mockUser.setRol("CLIENTE");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        boolean hasRole = authService.userHasRole("usuario@prueba.com", "ADMIN");

        assertFalse(hasRole);
    }

    @Test
    void testUserHasRole_UserNotExists() {
        when(accountService.findByEmail("noexiste@prueba.com")).thenReturn(null);

        boolean hasRole = authService.userHasRole("noexiste@prueba.com", "ADMIN");

        assertFalse(hasRole);
    }

    @Test
    void testUserHasRole_MultipleRoles() {
        mockUser.setRol("ADMIN");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        assertTrue(authService.userHasRole("usuario@prueba.com", "ADMIN"));
        assertFalse(authService.userHasRole("usuario@prueba.com", "CLIENTE"));
    }

    @Test
    void testValidateTokenAndRole_Success() {
        mockUser.setRol("ADMIN");
        when(jwtUtil.validateTokenAndGetClaims("validToken")).thenReturn(null);
        when(jwtUtil.validateAndGetUser("validToken")).thenReturn("usuario@prueba.com");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        boolean valid = authService.validateTokenAndRole("validToken", "ADMIN");

        assertTrue(valid);
    }

    @Test
    void testValidateTokenAndRole_InvalidToken() {
        when(jwtUtil.validateTokenAndGetClaims("invalidToken")).thenThrow(new RuntimeException("Invalid"));

        boolean valid = authService.validateTokenAndRole("invalidToken", "ADMIN");

        assertFalse(valid);
    }

    @Test
    void testValidateTokenAndRole_InvalidRole() {
        mockUser.setRol("CLIENTE");
        when(jwtUtil.validateTokenAndGetClaims("validToken")).thenReturn(null);
        when(jwtUtil.validateAndGetUser("validToken")).thenReturn("usuario@prueba.com");
        when(accountService.findByEmail("usuario@prueba.com")).thenReturn(mockUser);

        boolean valid = authService.validateTokenAndRole("validToken", "ADMIN");

        assertFalse(valid);
    }

    @Test
    void testValidateTokenAndRole_UserNotFound() {
        when(jwtUtil.validateTokenAndGetClaims("validToken")).thenReturn(null);
        when(jwtUtil.validateAndGetUser("validToken")).thenReturn("noexiste@prueba.com");
        when(accountService.findByEmail("noexiste@prueba.com")).thenReturn(null);

        boolean valid = authService.validateTokenAndRole("validToken", "ADMIN");

        assertFalse(valid);
    }
}
