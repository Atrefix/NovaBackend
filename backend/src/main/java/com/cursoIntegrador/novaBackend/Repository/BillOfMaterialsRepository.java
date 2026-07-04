package com.cursoIntegrador.novaBackend.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.BillOfMaterials;

@Repository
public interface BillOfMaterialsRepository extends JpaRepository<BillOfMaterials, Long> {
    Optional<BillOfMaterials> findByProduct_Idproducto(Integer idproducto);
}
