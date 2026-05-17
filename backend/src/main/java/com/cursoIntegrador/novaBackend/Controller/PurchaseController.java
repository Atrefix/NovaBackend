package com.cursoIntegrador.novaBackend.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.DTO.Purchase.PurchaseRequestDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Purchase.PurhcaseHistoryDTO;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Service.DAO.PurchaseService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/purchases")
@AllArgsConstructor
public class PurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    PurchaseService purchaseService;

    @GetMapping("/getPurchases")
    public ResponseEntity<?> getPurchases(@AuthenticationPrincipal CustomUserDetails userDetails) {

        logger.info("Intento de solicitar historial de compras de usuario: {}", userDetails.getUsername());

        try {
            List<PurhcaseHistoryDTO> purchases = purchaseService.getHistory(userDetails);
            logger.info("Historial de compras de {} obtenido", userDetails.getUsername());
            return ResponseEntity.ok(purchases);

        } catch (RuntimeException e) {
            logger.error("Error al obtener historial: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }

    }

    @PostMapping("/newPurchase")
    public ResponseEntity<?> newPurchase(@RequestBody PurchaseRequestDTO purchase,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        logger.info("Intento de registrar compra de usuario: {}", userDetails.getUsername());

        try {
            purchaseService.savePurchase(userDetails, purchase);
            logger.info("Compra registrada con exito para: {}", userDetails.getUsername());
            return ResponseEntity.ok(purchase);

        } catch (RuntimeException e) {
            logger.error("Error al crear compra: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
