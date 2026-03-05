package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cajas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Caja extends BaseEntity {
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montoInicial;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalVentas;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montoFinalEsperado;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal montoFinalReal;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;
    
    private String estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}