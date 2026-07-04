package com.cursoIntegrador.novaBackend.Model.Entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bill_of_materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la receta es obligatorio")
    private String nombre;

    private String descripcion;

    @OneToOne
    @JoinColumn(name = "idproducto", nullable = false, unique = true)
    @NotNull(message = "El producto terminado asociado a la receta es obligatorio")
    private Product product;

    @OneToMany(mappedBy = "billOfMaterials", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BOMDetail> details = new ArrayList<>();
}
