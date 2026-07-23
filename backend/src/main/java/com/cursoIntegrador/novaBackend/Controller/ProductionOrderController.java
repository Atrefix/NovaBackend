package com.cursoIntegrador.novaBackend.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.cursoIntegrador.novaBackend.Model.Entity.ProductionOrder;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Service.DAO.ProductionOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/production-orders")
public class ProductionOrderController {

    private static final Logger logger = LoggerFactory.getLogger(ProductionOrderController.class);

    @Autowired
    private ProductionOrderService orderService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getReport")
    public ResponseEntity<?> getReportProduction() throws Exception {
        byte[] pdf = orderService.getReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-produccion.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductionOrder>> getAllOrders() {
        logger.info("Solicitud para obtener todas las órdenes de producción");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ProductionOrder> getOrderById(@PathVariable Long id) {
        logger.info("Solicitud para obtener orden de producción con ID: {}", id);
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/newOrder")
    public ResponseEntity<?> createOrder(@Valid @RequestBody ProductionOrder order,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Intento de crear orden de producción por usuario: {}", userDetails.getUsername());
        try {
            ProductionOrder created = orderService.createOrder(userDetails, order);
            logger.info("Orden de producción creada con ID: {}", created.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("Error al crear orden de producción: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/start/{id}")
    public ResponseEntity<?> startOrder(@PathVariable Long id) {
        logger.info("Intento de iniciar producción para la orden ID: {}", id);
        try {
            ProductionOrder started = orderService.startOrder(id);
            logger.info("Producción iniciada para la orden ID: {}", id);
            return ResponseEntity.ok(started);
        } catch (Exception e) {
            logger.error("Error al iniciar producción: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/complete/{id}")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        logger.info("Intento de completar la orden de producción ID: {}", id);
        try {
            ProductionOrder completed = orderService.completeOrder(id);
            logger.info("Orden de producción ID {} completada exitosamente", id);
            return ResponseEntity.ok(completed);
        } catch (Exception e) {
            logger.error("Error al completar producción: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/reject/{id}")
    public ResponseEntity<?> rejectOrder(@PathVariable Long id) {
        logger.info("Intento de rechazar/cancelar la orden de producción ID: {}", id);
        try {
            ProductionOrder rejected = orderService.rejectOrder(id);
            logger.info("Orden de producción ID {} cancelada/rechazada", id);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            logger.error("Error al cancelar producción: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
