package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "configuracion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Configuracion {
    
    @Id
    private Long id = 1L; 

    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String telefono;
    private String correo;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeIgv;

    private String simboloMoneda;
    
    @Column(length = 500)
    private String mensajeTicket;
}