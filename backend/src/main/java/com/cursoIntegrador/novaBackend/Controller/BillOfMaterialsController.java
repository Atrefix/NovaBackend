package com.cursoIntegrador.novaBackend.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cursoIntegrador.novaBackend.Model.Entity.BillOfMaterials;
import com.cursoIntegrador.novaBackend.Service.DAO.BillOfMaterialsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/recipes")
public class BillOfMaterialsController {

    private static final Logger logger = LoggerFactory.getLogger(BillOfMaterialsController.class);

    @Autowired
    private BillOfMaterialsService bomService;

    @GetMapping("/getAll")
    public ResponseEntity<List<BillOfMaterials>> getAllRecipes() {
        logger.info("Solicitud para obtener todas las recetas (BOM)");
        return ResponseEntity.ok(bomService.getAllBOMs());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<BillOfMaterials> getRecipeById(@PathVariable Long id) {
        logger.info("Solicitud para obtener receta con ID: {}", id);
        try {
            return ResponseEntity.ok(bomService.getBOMById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<BillOfMaterials> getRecipeByProductId(@PathVariable Integer productId) {
        logger.info("Solicitud para obtener receta del producto con ID: {}", productId);
        try {
            return ResponseEntity.ok(bomService.getBOMByProductId(productId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRecipe(@Valid @RequestBody BillOfMaterials bom) {
        logger.info("Intento de crear receta para producto ID: {}", bom.getProduct().getIdproducto());
        try {
            BillOfMaterials saved = bomService.saveBOM(bom);
            logger.info("Receta creada exitosamente con ID: {}", saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error al crear receta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        logger.info("Intento de eliminar receta con ID: {}", id);
        try {
            bomService.deleteBOM(id);
            logger.info("Receta con ID {} eliminada", id);
            return ResponseEntity.ok("Receta eliminada correctamente");
        } catch (Exception e) {
            logger.error("Error al eliminar receta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
