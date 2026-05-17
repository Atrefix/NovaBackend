package com.cursoIntegrador.novaBackend.Controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.Entity.Branch;
import com.cursoIntegrador.novaBackend.Service.DAO.BranchService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sucursales")
public class BranchController {

    @Autowired
    private BranchService branchService;

    private static final Logger logger = LoggerFactory.getLogger(BranchController.class);

    @GetMapping("/listar")
    public ResponseEntity<List<Branch>> listarSucursales() {
        logger.info("Intento de solicitar lista de sucursales");
        try {
            List<Branch> sucursales = branchService.listarSucursales();
            logger.info("Lista de sucursales obtenidos");
            return ResponseEntity.ok(sucursales);
        } catch (Exception e) {
            logger.error("Error inesperado al listar sucursales: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/agregar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> agregarSucursal(@RequestBody Branch branch) {
        logger.info("Intento de agregar sucursal: {}", branch.getNombre());

        try {
            Branch nuevaSucursal = branchService.guardarSucursal(branch);
            logger.info("Sucursal agregada por admin: {}", nuevaSucursal.getNombre());
            return ResponseEntity.ok(nuevaSucursal);
        } catch (Exception e) {
            logger.error("Error al agregar sucursal: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al agregar sucursal");
        }
    }

    @PatchMapping("/modificar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modificarSucursal(
            @PathVariable Integer id,
            @RequestBody Branch branchActualizada) {

        logger.info("Intento de modificar sucursal ID: {}", id);

        try {
            Branch modificada = branchService.modificarSucursalParcial(id, branchActualizada);
            logger.info("Sucursal con ID {} modificada correctamente", id);
            return ResponseEntity.ok(modificada);

        } catch (IllegalArgumentException e) {
            logger.warn("Sucursal con ID {} no encontrada: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al modificar sucursal: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al modificar sucursal");
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarSucursal(@PathVariable Integer id) {

        logger.info("Intento de eliminar sucursal con ID: {}", id);

        try {
            branchService.eliminarSucursal(id);
            logger.info("Sucursal con ID {} eliminada correctamente", id);
            return ResponseEntity.ok("Sucursal eliminada correctamente");

        } catch (IllegalArgumentException e) {
            logger.warn("Sucursal no encontrada: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al eliminar sucursal: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al eliminar sucursal");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getReport")
    public ResponseEntity<?> getReportProduct() throws Exception {
        byte[] pdf = branchService.getReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
