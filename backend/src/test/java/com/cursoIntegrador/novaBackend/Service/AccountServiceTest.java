package com.cursoIntegrador.novaBackend.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountListDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountUpdateDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Repository.AccountRepository;
import com.cursoIntegrador.novaBackend.Service.DAO.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AccountService accountService;

    private Cuenta cuentaEjemplo;

    @BeforeEach
    void setUp() {
        cuentaEjemplo = new Cuenta();
        cuentaEjemplo.setEmail("usuario@prueba.com");
        cuentaEjemplo.setPassword("password123");
        cuentaEjemplo.setAlias("Usuario Test");
    }

    @Test
    void testSave_Success() {
        Cuenta user = new Cuenta();
        user.setEmail("newuser@test.com");

        accountService.save(user);

        assertEquals("ACTIVO", user.getEstado());
        assertEquals("CLIENTE", user.getRol());
        assertNotNull(user.getFechaRegistro());
        verify(accountRepository).save(user);
    }

    @Test
    void testSave_ValoresPorDefecto() {
        Cuenta user = new Cuenta();
        accountService.save(user);

        assertEquals("ACTIVO", user.getEstado());
        assertEquals("CLIENTE", user.getRol());
        assertNotNull(user.getFechaRegistro());
        assertTrue(user.getFechaRegistro().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testSave_PreservaOtrosAtributos() {
        Cuenta user = new Cuenta();
        user.setAlias("Juan");
        user.setEmail("juan@test.com");

        accountService.save(user);

        assertEquals("Juan", user.getAlias());
        assertEquals("juan@test.com", user.getEmail());
        assertEquals("ACTIVO", user.getEstado());
    }

    @Test
    void testFindByEmail_Success() {
        when(accountRepository.findByEmail("usuario@prueba.com")).thenReturn(cuentaEjemplo);

        Cuenta result = accountService.findByEmail("usuario@prueba.com");

        assertEquals("usuario@prueba.com", result.getEmail());
        verify(accountRepository).findByEmail("usuario@prueba.com");
    }

    @Test
    void testFindByEmail_NotFound() {
        when(accountRepository.findByEmail("noexiste@test.com")).thenReturn(null);

        Cuenta result = accountService.findByEmail("noexiste@test.com");

        assertTrue(result == null);
    }

    @Test
    void testUpdatePassword_Success() {
        when(accountRepository.findByEmail("usuario@prueba.com")).thenReturn(cuentaEjemplo);

        accountService.updatePassword("usuario@prueba.com", "nuevaPass");

        assertEquals("nuevaPass", cuentaEjemplo.getPassword());
        verify(accountRepository).save(cuentaEjemplo);
    }

    @Test
    void testUpdatePassword_UsuarioNoEncontrado() {
        when(accountRepository.findByEmail("noExiste@correo.com")).thenReturn(null);

        accountService.updatePassword("noExiste@correo.com", "nuevaPass");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void testUpdatePassword_ActualizaCorrectamente() {
        when(accountRepository.findByEmail("usuario@prueba.com")).thenReturn(cuentaEjemplo);

        accountService.updatePassword("usuario@prueba.com", "passCompleja123");

        assertEquals("passCompleja123", cuentaEjemplo.getPassword());
        verify(accountRepository, times(1)).save(cuentaEjemplo);
    }

    @Test
    void testListarUsuarios_Success() {
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo);
        when(accountRepository.findAll()).thenReturn(cuentas);

        List<AccountListDTO> result = accountService.listarUsuarios();

        assertEquals(1, result.size());
        verify(accountRepository).findAll();
    }

    @Test
    void testListarUsuarios_Empty() {
        when(accountRepository.findAll()).thenReturn(new ArrayList<>());

        List<AccountListDTO> result = accountService.listarUsuarios();

        assertEquals(0, result.size());
        verify(accountRepository).findAll();
    }

    @Test
    void testListarUsuarios_MultipleCuentas() {
        Cuenta cuenta2 = new Cuenta();
        cuenta2.setEmail("usuario2@prueba.com");

        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo, cuenta2);
        when(accountRepository.findAll()).thenReturn(cuentas);

        List<AccountListDTO> result = accountService.listarUsuarios();

        assertEquals(2, result.size());
    }

    @Test
    void testListarUsuarios_ConvierteACorrecto() {
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo);
        when(accountRepository.findAll()).thenReturn(cuentas);

        List<AccountListDTO> result = accountService.listarUsuarios();

        assertNotNull(result.get(0));
        assertEquals("usuario@prueba.com", result.get(0).getEmail());
    }

    @Test
    void testExportarExcel_Success() throws Exception {
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo);
        when(accountRepository.findAll()).thenReturn(cuentas);

        ByteArrayInputStream result = assertDoesNotThrow(() -> accountService.exportarExcel());

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    void testExportarExcel_WithMultipleCuentas() throws Exception {
        Cuenta cuenta2 = new Cuenta();
        cuenta2.setEmail("usuario2@test.com");
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo, cuenta2);
        when(accountRepository.findAll()).thenReturn(cuentas);

        ByteArrayInputStream result = assertDoesNotThrow(() -> accountService.exportarExcel());

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    void testUpdateAccountData_Success() {
        CustomUserDetails userDetails = new CustomUserDetails(cuentaEjemplo);
        AccountUpdateDTO dto = new AccountUpdateDTO();
        dto.setAlias("NombreActualizado");

        accountService.updateAccountData(userDetails, dto);

        verify(accountRepository).save(cuentaEjemplo);
    }

    @Test
    void testUpdateAccountData_UserDetailsNull() {
        CustomUserDetails userDetails = new CustomUserDetails(null);
        AccountUpdateDTO dto = new AccountUpdateDTO();

        assertDoesNotThrow(() -> accountService.updateAccountData(userDetails, dto));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testUpdateAccountData_PartialUpdate() {
        CustomUserDetails userDetails = new CustomUserDetails(cuentaEjemplo);
        AccountUpdateDTO dto = new AccountUpdateDTO();
        dto.setAlias("NuevoNombre");

        accountService.updateAccountData(userDetails, dto);

        verify(accountRepository).save(cuentaEjemplo);
    }

    @Test
    void testGetReport_Success() throws IllegalAccessException {
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo);
        when(accountRepository.findAll()).thenReturn(cuentas);
        when(reportService.generateExampleReport(any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = accountService.getReport();

        assertNotNull(result);
        assertEquals(3, result.length);
        verify(reportService).generateExampleReport(any(), any());
    }

    @Test
    void testGetReport_ExceptionHandling() throws IllegalAccessException {
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo);
        when(accountRepository.findAll()).thenReturn(cuentas);
        when(reportService.generateExampleReport(any(), any())).thenThrow(new IllegalAccessException());

        byte[] result = accountService.getReport();

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGetReport_WithMultipleCuentas() throws IllegalAccessException {
        Cuenta cuenta2 = new Cuenta();
        cuenta2.setEmail("usuario2@test.com");
        List<Cuenta> cuentas = Arrays.asList(cuentaEjemplo, cuenta2);
        when(accountRepository.findAll()).thenReturn(cuentas);
        when(reportService.generateExampleReport(any(), any())).thenReturn(new byte[]{1, 2, 3, 4, 5});

        byte[] result = accountService.getReport();

        assertNotNull(result);
        assertEquals(5, result.length);
    }
}
