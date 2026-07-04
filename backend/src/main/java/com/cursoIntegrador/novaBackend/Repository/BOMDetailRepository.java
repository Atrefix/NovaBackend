package com.cursoIntegrador.novaBackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.BOMDetail;

@Repository
public interface BOMDetailRepository extends JpaRepository<BOMDetail, Long> {
}
