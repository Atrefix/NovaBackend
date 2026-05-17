package com.cursoIntegrador.novaBackend.Service.DAO;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.io.IOException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountListDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountUpdateDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Account.ChangeRoleRequestDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Repository.AccountRepository;
import com.cursoIntegrador.novaBackend.Service.ReportService;
import com.cursoIntegrador.novaBackend.Util.ExcelGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private AccountRepository accountRepository;

    public Cuenta findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public void save(Cuenta user) {
        user.setEstado("ACTIVO");
        user.setRol("CLIENTE");
        user.setFechaRegistro(LocalDateTime.now());
        accountRepository.save(user);
    }

    public void updatePassword(String email, String nuevaPassword) {
        Cuenta cuenta = accountRepository.findByEmail(email);
        if (cuenta != null) {
            cuenta.setPassword(nuevaPassword);
            accountRepository.save(cuenta);
        }
    }

    public List<AccountListDTO> listarUsuarios() {
        List<Cuenta> cuentas = accountRepository.findAll();
        List<AccountListDTO> cuentasDTO = new ArrayList<>();

        for (Cuenta element : cuentas) {
            AccountListDTO elementDTO = new AccountListDTO(element);
            cuentasDTO.add(elementDTO);
        }

        return cuentasDTO;
    }

    public ByteArrayInputStream exportarExcel() throws IOException {
        List<AccountListDTO> cuentas = this.listarUsuarios();
        return ExcelGenerator.generateExcel(cuentas, "Cuentas");
    }

    public void updateAccountData(CustomUserDetails userDetails, AccountUpdateDTO dto) {
        Cuenta cuenta = userDetails.getCuenta();
        if (cuenta == null)
            return;

        String[] nullProps = getNullPropertyNames(dto);
        BeanUtils.copyProperties(dto, cuenta, nullProps);

        accountRepository.save(cuenta);
    }

    private String[] getNullPropertyNames(Object source) {
        final var src = new BeanWrapperImpl(source);
        return Stream.of(src.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    public byte[] getReport() {
        try {
            return reportService.generateExampleReport(this.listarUsuarios(), "USUARIOS");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public ChangeRoleRequestDTO cambiarRol(ChangeRoleRequestDTO changeRoleRequestDTO) {

        Cuenta cuenta = accountRepository.findById(changeRoleRequestDTO.getIdcuenta())
                .orElse(null);

        if (cuenta == null) {
            throw new RuntimeException("Cuenta no encontrada");
        }

        List<String> rolesPermitidos = List.of("ADMIN", "CLIENTE");

        if (!rolesPermitidos.contains(changeRoleRequestDTO.getRol())) {
            throw new RuntimeException("Rol inválido");
        }

        cuenta.setRol(changeRoleRequestDTO.getRol());
        accountRepository.save(cuenta);

        return new ChangeRoleRequestDTO(cuenta.getIdcuenta(), cuenta.getRol());
    }
}
