package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.Caja;
import org.springframework.data.jpa.repository.EntityGraph;
import com.menestrasvilla.gestion.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {
    Optional<Caja> findByUsuarioAndEstado(Usuario usuario, String estado);
    Optional<Caja> findFirstByUsuarioAndEstadoOrderByFechaAperturaDesc(Usuario usuario, String estado);
    long countByEstadoAndFechaCierreAfter(String estado, LocalDateTime fecha);
    @EntityGraph(attributePaths = {"usuario"})
    List<Caja> findByFechaCierreBetweenAndEstadoOrderByFechaCierreDesc(LocalDateTime inicio, LocalDateTime fin, String estado);
}