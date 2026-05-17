package com.cursoIntegrador.novaBackend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryService {
    @Autowired
    private TokenCache tokenCache;
    @Autowired
    private EmailService emailService;

    public void generarYEnviarToken(String email) throws Exception {
        String token = java.util.UUID.randomUUID().toString();
        tokenCache.guardarToken(email, token);
        emailService.enviarTokenDeRecuperacion(email, token);
    }

    public boolean validarToken(String email, String tokenIngresado) {
        String tokenGuardado = tokenCache.obtenerToken(email);
        return tokenGuardado != null && tokenGuardado.equals(tokenIngresado);
    }

    public void invalidarToken(String email) {
        tokenCache.eliminarToken(email);
    }
}
