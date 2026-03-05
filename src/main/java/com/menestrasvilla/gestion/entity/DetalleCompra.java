package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetalleCompra extends BaseEntity {
    private BigDecimal cantidadKg;
    private BigDecimal precioUnidadCompra;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
}