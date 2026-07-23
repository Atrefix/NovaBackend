package com.cursoIntegrador.novaBackend.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bom_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BOMDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_bom", nullable = false)
    @NotNull(message = "La receta asociada es obligatoria")
    @JsonIgnore
    private BillOfMaterials billOfMaterials;

    @ManyToOne
    @JoinColumn(name = "idproducto", nullable = false)
    @NotNull(message = "El insumo/ingrediente es obligatorio")
    private Product ingredient;

    @NotNull(message = "La cantidad requerida es obligatoria")
    @Positive(message = "La cantidad requerida debe ser mayor que cero")
    private Integer quantityRequired;
}
