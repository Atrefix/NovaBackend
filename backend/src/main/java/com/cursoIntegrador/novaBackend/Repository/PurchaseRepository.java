package com.cursoIntegrador.novaBackend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findAllByCuentaIdcuenta(Integer idCuenta);
}
