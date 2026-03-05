package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pagos_venta")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PagoVenta extends BaseEntity {
    private String metodoPago;
    private BigDecimal monto;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;
}