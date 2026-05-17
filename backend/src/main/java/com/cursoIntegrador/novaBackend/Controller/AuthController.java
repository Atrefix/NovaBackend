package com.cursoIntegrador.novaBackend.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.DTO.Login.LoginRequest;
import com.cursoIntegrador.novaBackend.Model.DTO.Login.PasswordChangeRequest;
import com.cursoIntegrador.novaBackend.Service.AuthService;
import com.cursoIntegrador.novaBackend.Service.PasswordRecoveryService;
import com.cursoIntegrador.novaBackend.Service.WelcomeService;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordRecoveryService passwordRecoveryService;

    @Autowired
    private WelcomeService welcomeService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getUsername();
        logger.info("Intento de login para: {}", email);
        try {
            var response = authService.login(email, loginRequest.getPassword());
            logger.debug("Login exitoso para: {}", email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Login fallido para: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/protectedTest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> protectedTest() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok("USUARIO : " + username + " ENTRO AL ENDPOINT PROTEGIDO");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getUsername();
        logger.info("Intento de registro para: {}", email);

        try {
            authService.register(email, loginRequest.getPassword());
            logger.debug("Registro exitoso para: {}", email);
            welcomeService.enviarBienvenida(email);
            Map<String, Object> response = authService.login(email, loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Error en registro de {} - {}", email, e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = authService.extractUsername(token);
            authService.invalidateToken(token);
            logger.info("Logout exitoso para: {}", username);
            return ResponseEntity.ok("Token invalidado, funcionó el logout");
        } catch (RuntimeException e) {
            logger.error("Error al hacer logout: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Error al hacer logout");
        }
    }

    @PostMapping("/recuperar")
    public ResponseEntity<String> enviarToken(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        logger.info("Solicitud de recuperación de contraseña para: {}", email);

        if (!authService.userExists(email)) {
            logger.warn("Intento de recuperación de contraseña para email no registrado: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe ninguna cuenta asociada a este correo");
        }

        try {
            passwordRecoveryService.generarYEnviarToken(email);
            logger.debug("Token de recuperación generado y enviado a {}", email);
            return ResponseEntity.ok("Se envió un token de recuperación al correo " + email);
        } catch (Exception e) {
            logger.error("Error al enviar token de recuperación a {} - {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar token: " + e.getMessage());
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<Map<String, Object>> cambiarPassword(@RequestBody PasswordChangeRequest request) {

        String email = request.getEmail();
        logger.info("Intento de cambio de contraseña para: {}", email);
        Map<String, Object> respuesta = new HashMap<>();
        boolean valido = passwordRecoveryService.validarToken(email, request.getToken());

        if (!valido) {
            logger.warn("Token inválido o expirado en intento de cambio de contraseña para: {}", email);
            respuesta.put("valido", false);
            respuesta.put("mensaje", "Token inválido o expirado, no se cambió la contraseña");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
        }

        try {
            authService.actualizarPassword(email, request.getNuevaPassword());
            passwordRecoveryService.invalidarToken(email);

            logger.debug("Contraseña actualizada correctamente para: {}", email);
            respuesta.put("valido", true);
            respuesta.put("mensaje", "Contraseña actualizada correctamente");
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al cambiar la contraseña para {} - {}", email, e.getMessage(), e);
            respuesta.put("valido", false);
            respuesta.put("mensaje", "Error al cambiar la contraseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }
}
