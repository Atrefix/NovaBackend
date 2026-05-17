package com.cursoIntegrador.novaBackend.Model.DTO.Purchase;

import java.math.BigDecimal;

import com.cursoIntegrador.novaBackend.Model.Entity.PurchaseDetails;

import lombok.Data;

@Data
public class PurchaseHistoryDetailDTO {
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String instructions;

    public PurchaseHistoryDetailDTO(PurchaseDetails purchaseDetail) {
        this.productId = purchaseDetail.getProduct().getIdproducto();
        this.productName = purchaseDetail.getProduct().getNombre();
        this.quantity = purchaseDetail.getQuantity();
        this.price = purchaseDetail.getProduct().getPrecioventa();
        this.instructions = purchaseDetail.getInstructions();
    }

}
