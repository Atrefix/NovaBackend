package com.cursoIntegrador.novaBackend.Model.DTO.Notifications;

import java.time.LocalDateTime;

import com.cursoIntegrador.novaBackend.Model.Entity.Notification;

import lombok.Data;

@Data
public class NotificationDTO {

    private Long idNotificaciones;

    private String asunto;

    private String descripcion;

    private LocalDateTime fechaHoraEnvio;

    public NotificationDTO(Notification notification) {
        this.idNotificaciones = notification.getIdNotificaciones();
        this.asunto = notification.getAsunto();
        this.descripcion = notification.getDescripcion();
        this.fechaHoraEnvio = notification.getFechaHoraEnvio();
    }

}
