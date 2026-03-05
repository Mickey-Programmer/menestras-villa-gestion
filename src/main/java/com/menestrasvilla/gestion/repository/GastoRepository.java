package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.Caja;
import com.menestrasvilla.gestion.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {
    
    List<Gasto> findAllByOrderByFechaPagoDesc();
    
    List<Gasto> findByCaja(Caja caja);
    
    List<Gasto> findByFechaPagoBetweenOrderByFechaPagoDesc(LocalDate inicio, LocalDate fin);

    @Query("SELECT SUM(g.monto) FROM Gasto g")
    BigDecimal sumarTotalGastosGlobal();
    
    @Query("SELECT SUM(g.monto) FROM Gasto g WHERE g.fechaPago >= :inicio AND g.fechaPago <= :fin")
    BigDecimal sumarTotalGastosPorFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDate inicio, @org.springframework.data.repository.query.Param("fin") java.time.LocalDate fin);
}