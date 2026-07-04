package com.cursoIntegrador.novaBackend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.ProductionOrder;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    List<ProductionOrder> findAllBySupervisor_Idcuenta(Integer idcuenta);
    List<ProductionOrder> findAllByBranch_Idsucursal(Integer idsucursal);
}
