package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.categoria.activo = true")
    List<Producto> findProductosParaVenta();
    
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockActualKg <= p.stockMinimoKg AND p.activo = true")
    long contarProductosConBajoStock();
    
    @Query("SELECT p FROM Producto p WHERE p.stockActualKg <= p.stockMinimoKg AND p.activo = true")
    List<Producto> findProductosConBajoStock();
    
    boolean existsByNombre(String nombre);
}