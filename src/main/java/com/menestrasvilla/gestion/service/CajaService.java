package com.menestrasvilla.gestion.service;

import com.menestrasvilla.gestion.entity.Caja;
import com.menestrasvilla.gestion.entity.Gasto;
import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.repository.CajaRepository;
import com.menestrasvilla.gestion.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CajaService {

    private final CajaRepository cajaRepository;
    private final GastoRepository gastoRepository;

    public Caja obtenerCajaAbierta(Usuario usuario) {
        return cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA").orElse(null);
    }

    @Transactional
    public Caja abrirCaja(Caja caja, Usuario usuario) {
        if (obtenerCajaAbierta(usuario) != null) {
            throw new RuntimeException("El usuario ya tiene un turno de caja abierto.");
        }
        caja.setUsuario(usuario);
        caja.setEstado("ABIERTA");
        caja.setFechaApertura(LocalDateTime.now());
        return cajaRepository.save(caja);
    }

    @Transactional
    public Gasto registrarGasto(Gasto gasto, Usuario usuario) {
        Caja cajaActiva = obtenerCajaAbierta(usuario);
        if (cajaActiva == null) throw new RuntimeException("Debe abrir caja para registrar gastos.");
        
        gasto.setCaja(cajaActiva);
        return gastoRepository.save(gasto);
    }
    
    @Transactional
    public Caja cerrarCaja(Caja caja, BigDecimal montoReal) {
        caja.setEstado("CERRADA");
        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoFinalReal(montoReal);
        return cajaRepository.save(caja);
    }
}