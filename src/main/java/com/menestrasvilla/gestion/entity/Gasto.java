package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gastos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Gasto extends BaseEntity {
    
    private String descripcion;
    private BigDecimal monto;
    private String responsable;
    
    private String categoria;
    private String frecuencia;
    private LocalDate fechaPago;

    @ManyToOne
    @JoinColumn(name = "caja_id")
    private Caja caja;
}