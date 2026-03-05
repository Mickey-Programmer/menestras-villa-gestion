package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    
    @EntityGraph(attributePaths = {"producto", "usuario"})
    List<MovimientoInventario> findAllByOrderByFechaHoraDesc();
}