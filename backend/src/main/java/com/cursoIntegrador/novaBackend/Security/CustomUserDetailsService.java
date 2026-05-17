package com.cursoIntegrador.novaBackend.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Service.DAO.AccountService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Cuenta cuenta = accountService.findByEmail(email);

        if (cuenta == null) {
            throw new UsernameNotFoundException("Usuario no encontrado:" + email);
        }

        return new CustomUserDetails(cuenta);
    }
}
