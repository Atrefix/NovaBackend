package com.cursoIntegrador.novaBackend.Controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.DTO.Notifications.NotificationDTO;
import com.cursoIntegrador.novaBackend.Service.DAO.NotificationService;

@RequestMapping("/notificaciones")
@RestController
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/listar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listarNotificaciones(Principal principal) {
        try {
            String email = principal.getName();
            logger.info("Buscando notificaciones del email: {}", email);

            List<NotificationDTO> notificaciones = notificationService.getAllUserNotis(email);
            logger.info("Notificaciones encontradas: {}", notificaciones.size());

            if (notificaciones.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(notificaciones);

        } catch (Exception e) {
            logger.error("Error al listar notificaciones: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al listar notificaciones");
        }
    }

}
