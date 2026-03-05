package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetalleVenta extends BaseEntity {
    private String unidadMedida;
    private BigDecimal cantidadUnidades;
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
}