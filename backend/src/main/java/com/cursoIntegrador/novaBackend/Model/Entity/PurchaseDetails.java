package com.cursoIntegrador.novaBackend.Model.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchasedetails")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PurchaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idpurchase")
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "idproducto")
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = true)
    private String instructions;
}
