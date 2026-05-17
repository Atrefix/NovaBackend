package com.cursoIntegrador.novaBackend.Controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountListDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountUpdateDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Account.ChangeRoleRequestDTO;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Service.DAO.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<?> exportarExcel() throws IOException {

        ByteArrayInputStream excelStream = accountService.exportarExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=cuentas.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelStream));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar")
    public ResponseEntity<?> listarUsuarios() {

        List<AccountListDTO> cuentas = accountService.listarUsuarios();

        if (cuentas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cuentas);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<?> actualizarPerfil(@AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountUpdateDTO dto) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        accountService.updateAccountData(userDetails, dto);
        return ResponseEntity.ok("Perfil actualizado correctamente");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getReport")
    public ResponseEntity<?> getReportProduct() throws Exception {
        byte[] pdf = accountService.getReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PatchMapping("/changeRole")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarParcial(@RequestBody ChangeRoleRequestDTO changeRoleRequestDTO) {
        try {
            ChangeRoleRequestDTO actualizado = accountService.cambiarRol(changeRoleRequestDTO);
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar producto");
        }
    }

}
