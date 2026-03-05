package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.DetalleVenta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    
    @Query("SELECT d.producto.nombre, SUM(d.cantidadUnidades) as total FROM DetalleVenta d GROUP BY d.producto.nombre ORDER BY total DESC")
    List<Object[]> findProductosMasVendidos(Pageable pageable);
    
    @Query("SELECT d.producto.nombre, SUM(d.cantidadUnidades) " +
            "FROM DetalleVenta d " +
            "WHERE d.venta.fecha >= :inicio AND d.venta.fecha <= :fin " +
            "GROUP BY d.producto.nombre " +
            "ORDER BY SUM(d.cantidadUnidades) DESC")
     List<Object[]> findProductosMasVendidosPorFechas(
         @org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio, 
         @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin, 
         org.springframework.data.domain.Pageable pageable
     );
}