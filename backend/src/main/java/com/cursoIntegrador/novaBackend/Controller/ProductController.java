package com.cursoIntegrador.novaBackend.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.DTO.Product.ProductDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Product;
import com.cursoIntegrador.novaBackend.Service.DAO.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping("/getAllProducts")
    public ResponseEntity<List<ProductDTO>> getAllProductsWithImage(HttpServletRequest request) {

        String baseUrl = String.format("%s://%s:%d/images/productos/", request.getScheme(), request.getServerName(),
                request.getServerPort());

        logger.info("Solicitud para obtener todos los productos desde {}", baseUrl);

        try {
            List<Product> products = productService.getAllProducts();
            List<ProductDTO> productsDTO = new ArrayList<>();

            if (products.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            for (Product product : products) {
                ProductDTO productDTO = new ProductDTO(product);
                String imageUrl = baseUrl + product.getCodproducto() + ".webp";
                productDTO.setImageUrl(imageUrl);
                productsDTO.add(productDTO);
            }

            return ResponseEntity.ok(productsDTO);
        } catch (Exception e) {
            logger.error("Error al obtener productos - {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/agregar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> agregarProducto(@Valid @RequestBody Product product) {
        logger.info("Intento de agregar producto: {}", product.getNombre());

        try {
            Product nuevo = productService.guardarProducto(product);
            logger.info("Producto agregado por ADMIN : {}", nuevo.getNombre());
            return ResponseEntity.ok(nuevo);

        } catch (RuntimeException e) {
            logger.error("Error al agregar producto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al agregar producto");
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        logger.info("Intento de eliminar producto con ID: {}", id);
        try {
            productService.eliminarProductoPorId(id);
            logger.info("Producto con ID {} eliminado por admin", id);
            return ResponseEntity.ok("Producto eliminado correctamente");

        } catch (IllegalArgumentException e) {
            logger.warn("Intento de eliminar producto inexistente: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (Exception e) {
            logger.error("Error al eliminar producto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al eliminar producto");
        }
    }

    @PatchMapping("/modificar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarParcial(@PathVariable Integer id, @RequestBody Product productoParcial) {
        logger.info("Intento de actualización parcial de producto con ID: {}", id);
        try {
            Product actualizado = productService.modificarProducto(id, productoParcial);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            logger.error("Error en actualización parcial: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al actualizar producto");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getReport")
    public ResponseEntity<?> getReportProduct() throws Exception {
        byte[] pdf = productService.getReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
