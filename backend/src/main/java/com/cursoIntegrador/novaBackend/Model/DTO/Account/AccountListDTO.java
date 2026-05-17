package com.cursoIntegrador.novaBackend.Model.DTO.Account;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;

import lombok.Data;

@Data
public class AccountListDTO {

    private Integer idcuenta;
    private String email;
    private String rol;
    private String estado;
    private LocalDateTime fechaRegistro;
    private String alias;
    private String direccion;
    private String pais;
    private LocalDate fechaNacimiento;
    private String telefono;

    public AccountListDTO(Cuenta cuenta) {
        this.idcuenta = cuenta.getIdcuenta();
        this.email = cuenta.getEmail();
        this.rol = cuenta.getRol();
        this.estado = cuenta.getEstado();
        this.fechaRegistro = cuenta.getFechaRegistro();
        this.alias = cuenta.getAlias();
        this.direccion = cuenta.getDireccion();
        this.pais = cuenta.getPais();
        this.fechaNacimiento = cuenta.getFechaNacimiento();
        this.telefono = cuenta.getTelefono();
    }

}
