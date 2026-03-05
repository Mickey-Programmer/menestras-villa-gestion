package com.menestrasvilla.gestion.service;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final InventarioService inventarioService;
    private final CajaService cajaService;

    @Transactional
    public Venta procesarVenta(Venta venta, Usuario usuario) {
        Caja cajaActiva = cajaService.obtenerCajaAbierta(usuario);
        if (cajaActiva == null) {
            throw new RuntimeException("No puede vender. Debe abrir caja primero.");
        }

        venta.setCaja(cajaActiva);
        venta.setUsuario(usuario);

        for (DetalleVenta detalle : venta.getDetalles()) {
            detalle.setVenta(venta);
            inventarioService.actualizarStock(
                    detalle.getProducto().getId(), 
                    detalle.getCantidadUnidades(), 
                    detalle.getUnidadMedida(), 
                    true 
            );
        }

        for (PagoVenta pago : venta.getPagos()) {
            pago.setVenta(venta);
        }

        return ventaRepository.save(venta);
    }
}