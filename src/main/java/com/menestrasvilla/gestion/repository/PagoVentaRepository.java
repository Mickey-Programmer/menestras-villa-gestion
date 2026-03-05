package com.menestrasvilla.gestion.repository;

import com.menestrasvilla.gestion.entity.PagoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoVentaRepository extends JpaRepository<PagoVenta, Long> {
}