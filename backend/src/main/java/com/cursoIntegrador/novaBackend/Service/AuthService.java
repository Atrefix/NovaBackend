package com.cursoIntegrador.novaBackend.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.DTO.Account.AccountLoginDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Security.JwtUtil;
import com.cursoIntegrador.novaBackend.Service.DAO.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    @Autowired
    private AuthenticationManager authenticationManager;

    // public Map<String, Object> login(String username, String password) {
    // Cuenta user = accountService.findByEmail(username);
    // if (this.userExists(username) && passwordEncoder.matches(password,
    // user.getPassword())) {
    // Map<String, Object> respuesta = new ConcurrentHashMap<>();
    // String token = jwtUtil.generateToken(user.getEmail());
    // Cuenta cuenta = accountService.findByEmail(username);
    // respuesta.put("loginData", new AccountLoginDTO(cuenta, token));
    // return respuesta;
    // }
    // throw new RuntimeException("Credenciales inválidas");
    // }

    public Map<String, Object> login(String username, String password) {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(user.getUsername());

        Cuenta cuenta = accountService.findByEmail(username);
        Map<String, Object> respuesta = new ConcurrentHashMap<>();
        respuesta.put("loginData", new AccountLoginDTO(cuenta, token));

        return respuesta;
    }

    public String extractUsername(String token) {
        return jwtUtil.validateAndGetUser(token);
    }

    public void register(String email, String password) {
        if (this.userExists(email)) {
            throw new RuntimeException("El usuario ya está registrado");
        }
        String encriptada = passwordEncoder.encode(password);

        Cuenta newUser = new Cuenta();
        newUser.setEmail(email);
        newUser.setPassword(encriptada);
        accountService.save(newUser);
    }

    public boolean userExists(String username) {
        return accountService.findByEmail(username) != null;
    }

    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    public boolean isTokenValid(String token) {
        if (invalidatedTokens.contains(token)) {
            return false;
        }

        try {
            jwtUtil.validateTokenAndGetClaims(token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void actualizarPassword(String email, String newPassword) {
        Cuenta user = accountService.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        String encriptada = passwordEncoder.encode(newPassword);
        accountService.updatePassword(email, encriptada);
    }

    public boolean userHasRole(String email, String searchRole) {

        if (userExists(email)) {
            Cuenta user = accountService.findByEmail(email);
            String role = user.getRol();
            return role.equals(searchRole);
        }

        return false;
    }

    public boolean validateTokenAndRole(String token, String role) {
        if (this.isTokenValid(token)) {
            String username = this.extractUsername(token);
            return this.userHasRole(username, role);
        }
        return false;
    }
}
