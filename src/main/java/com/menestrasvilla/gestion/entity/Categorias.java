package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Categorias extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    public Categorias(String nombre) {
        this.nombre = nombre;
        this.activo = true;
    }
}