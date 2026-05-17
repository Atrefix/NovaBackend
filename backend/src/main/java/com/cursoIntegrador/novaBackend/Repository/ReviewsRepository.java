package com.cursoIntegrador.novaBackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cursoIntegrador.novaBackend.Model.Entity.Reviews;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Long> {

}
