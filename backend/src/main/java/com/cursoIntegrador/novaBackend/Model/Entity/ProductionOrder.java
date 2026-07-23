package com.cursoIntegrador.novaBackend.Model.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "production_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idproducto", nullable = false)
    @NotNull(message = "El producto a fabricar es obligatorio")
    private Product product;

    @NotNull(message = "La cantidad a fabricar es obligatoria")
    @Positive(message = "La cantidad a fabricar debe ser mayor que cero")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionOrderStatus status = ProductionOrderStatus.PENDIENTE;

    @Column(name = "date_created")
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(name = "date_started")
    private LocalDateTime dateStarted;

    @Column(name = "date_completed")
    private LocalDateTime dateCompleted;

    @ManyToOne
    @JoinColumn(name = "idcuenta", nullable = false)
    @NotNull(message = "El supervisor es obligatorio")
    private Cuenta supervisor;

    @ManyToOne
    @JoinColumn(name = "idsucursal", nullable = false)
    @NotNull(message = "La sucursal es obligatoria")
    private Branch branch;
}
