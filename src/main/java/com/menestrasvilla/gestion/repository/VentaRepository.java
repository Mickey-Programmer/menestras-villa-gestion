package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.Caja;
import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.entity.Venta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    List<Venta> findByCaja(Caja caja);
    
    @Query("SELECT v.tipoComprobante, COUNT(v) FROM Venta v GROUP BY v.tipoComprobante")
    List<Object[]> contarVentasPorTipoComprobante();
    
    @Query("SELECT SUM(v.totalNeto) FROM Venta v WHERE v.caja.id = :cajaId")
    BigDecimal sumarTotalNetoPorCaja(@Param("cajaId") Long cajaId);
    
    @Query("SELECT SUM(v.totalNeto) FROM Venta v")
    BigDecimal sumarTotalVentasGlobal();
    
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles WHERE v.id = :id")
    Optional<Venta> findByIdConDetalles(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    List<Venta> findByFechaBetweenAndTipoComprobanteOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin, String tipo);

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);
    
    Page<Venta> findByUsuarioOrderByFechaDesc(Usuario usuario, Pageable pageable);
    
    @Query("SELECT SUM(v.totalNeto) FROM Venta v WHERE v.fecha >= :inicio AND v.fecha <= :fin")
    BigDecimal sumarTotalNetoPorFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio, @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);

    @Query("SELECT v.tipoComprobante, COUNT(v) FROM Venta v WHERE v.fecha >= :inicio AND v.fecha <= :fin GROUP BY v.tipoComprobante")
    List<Object[]> contarVentasPorTipoComprobantePorFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio, @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);
}