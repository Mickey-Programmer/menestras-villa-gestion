package com.menestrasvilla.gestion.service;

import com.menestrasvilla.gestion.entity.Compra;
import com.menestrasvilla.gestion.entity.DetalleCompra;
import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.repository.CompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final InventarioService inventarioService;

    @Transactional
    public Compra registrarCompra(Compra compra, Usuario usuario) {
        compra.setUsuario(usuario);

        for (DetalleCompra detalle : compra.getDetalles()) {
            detalle.setCompra(compra);
            inventarioService.actualizarStock(
                    detalle.getProducto().getId(), 
                    detalle.getCantidadKg(), 
                    "KILO", 
                    false
            );
        }

        return compraRepository.save(compra);
    }
}