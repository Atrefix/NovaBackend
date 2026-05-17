package com.cursoIntegrador.novaBackend.Model.DTO.Account;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountUpdateDTO {
    private String alias;
    private String direccion;
    private String pais;
    private LocalDate fechaNacimiento;
    private String telefono;
}
