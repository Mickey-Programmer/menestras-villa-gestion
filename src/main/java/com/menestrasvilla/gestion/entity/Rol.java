package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Rol extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String nombre;
}