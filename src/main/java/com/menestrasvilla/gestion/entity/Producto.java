package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Producto extends BaseEntity {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;
    
    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock_actual_kg")
    private BigDecimal stockActualKg;
    
    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo_kg")
    private BigDecimal stockMinimoKg;
    
    @NotNull(message = "El precio por kilo es obligatorio")
    @Min(value = 1, message = "El precio debe ser al menos S/ 1.00")
    private BigDecimal precioKilo;

    @NotNull(message = "El precio por saco es obligatorio")
    @Min(value = 1, message = "El precio debe ser al menos S/ 1.00")
    private BigDecimal precioSaco;
    
    @NotNull(message = "El peso por saco es obligatorio")
    @Min(value = 1, message = "El peso por saco debe ser al menos 1 kg")
    @Column(name = "kg_por_saco")
    private BigDecimal kgPorSaco;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "Debe seleccionar una categoría para el producto")
    private Categorias categoria;
    
    @Column(nullable = false)
    private boolean activo = true;
}