package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.Compra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    
    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario", "proveedor"})
    List<Compra> findByCreadoEnBetweenOrderByCreadoEnDesc(LocalDateTime inicio, LocalDateTime fin);
    
    @Query("SELECT SUM(c.totalCompra) FROM Compra c WHERE c.usuario.id = :usuarioId AND c.creadoEn >= :fechaApertura")
    java.math.BigDecimal sumarTotalComprasPorUsuarioYFecha(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId, @org.springframework.data.repository.query.Param("fechaApertura") java.time.LocalDateTime fechaApertura);
}