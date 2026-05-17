package com.cursoIntegrador.novaBackend.Service;

import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WelcomeService {

    @Autowired
    private EmailService emailService;

    public void enviarBienvenida(String email) {
        try {
            emailService.enviarCorreoBienvenida(email);
        } catch (EmailException e) {
            System.err.println("No se pudo enviar correo de bienvenida a " + email + ": " + e.getMessage());
        }
    }
}
