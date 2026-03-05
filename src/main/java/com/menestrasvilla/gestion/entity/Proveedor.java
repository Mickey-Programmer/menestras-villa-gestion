package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Proveedor extends BaseEntity {
    private String ruc;
    private String razonSocial;
    private String contacto;
    private String telefono;
}