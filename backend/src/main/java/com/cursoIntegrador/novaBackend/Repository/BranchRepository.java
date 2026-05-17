package com.cursoIntegrador.novaBackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch,Integer>{
    
}
