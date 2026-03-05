package com.menestrasvilla.gestion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Venta extends BaseEntity {
    private BigDecimal totalBruto;
    private BigDecimal descuentoTotal;
    private String motivoDescuento;
    private BigDecimal totalNeto;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<PagoVenta> pagos;

    @Column(name = "tipo_comprobante")
    private String tipoComprobante;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "cliente_doc")
    private String clienteDoc;

    @Column(name = "metodo_pago")
    private String metodoPago;
    
    @Column(name = "fecha")
    private LocalDateTime fecha;
}