package com.cursoIntegrador.novaBackend.Model.Security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;

public class CustomUserDetails implements UserDetails {

    private final Cuenta cuenta;

    public CustomUserDetails(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + cuenta.getRol()));
    }

    @Override
    public String getPassword() {
        return cuenta.getPassword();
    }

    @Override
    public String getUsername() {
        return cuenta.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return "ACTIVO".equalsIgnoreCase(cuenta.getEstado());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVO".equalsIgnoreCase(cuenta.getEstado());
    }

    public Cuenta getCuenta() {
        return cuenta;
    }
}
