package com.cursoIntegrador.novaBackend.Model.DTO.Purchase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.cursoIntegrador.novaBackend.Model.Entity.Purchase;
import com.cursoIntegrador.novaBackend.Model.Entity.PurchaseDetails;

import lombok.Data;

@Data
public class PurhcaseHistoryDTO {
    private Long id;
    private String cityDelivery;
    private String addressDelivery;
    private BigDecimal total;
    private List<PurchaseHistoryDetailDTO> detalles = new ArrayList<>();;

    public PurhcaseHistoryDTO(Purchase purchase) {
        this.id = purchase.getIdpurchase();
        this.cityDelivery = purchase.getCityDelivery();
        this.addressDelivery = purchase.getAddressDelivery();
        this.total = purchase.getTotalAmount();

        for (PurchaseDetails detail : purchase.getDetails()) {

            this.detalles.add(new PurchaseHistoryDetailDTO(detail));

        }
    }

}
