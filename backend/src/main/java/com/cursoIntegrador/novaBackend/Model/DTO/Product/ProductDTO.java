package com.cursoIntegrador.novaBackend.Model.DTO.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cursoIntegrador.novaBackend.Model.Entity.Product;
import com.cursoIntegrador.novaBackend.Model.Entity.ProductType;

import lombok.Data;

@Data
public class ProductDTO {
    private Integer id;
    private String codproducto;
    private String nombre;
    private String categoria;
    private Integer stock;
    private BigDecimal precioventa;
    private LocalDateTime fechavencimiento;
    private String imageUrl;
    private ProductType tipoProducto;

    public ProductDTO(Product product) {
        this.id = product.getIdproducto();
        this.codproducto = product.getCodproducto();
        this.nombre = product.getNombre();
        this.categoria = product.getCategoria();
        this.stock = product.getStock();
        this.precioventa = product.getPrecioventa();
        this.fechavencimiento = product.getFechavencimiento();
        this.tipoProducto = product.getTipoProducto();
    }
}
