package com.cursoIntegrador.novaBackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;

@Repository
public interface AccountRepository extends JpaRepository<Cuenta, Integer> {

    Cuenta findByEmail(String email);

    Cuenta findByEmailAndPassword(String email, String password);

}
