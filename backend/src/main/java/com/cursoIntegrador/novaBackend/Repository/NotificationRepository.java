package com.cursoIntegrador.novaBackend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByCuentaEmail(String email);
}
