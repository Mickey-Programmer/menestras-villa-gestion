package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final GastoRepository gastoRepository;
    private final CompraRepository compraRepository;

    @GetMapping("/estado")
    public String verificarEstado(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Caja cajaActiva = cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA").orElse(null);
        
        if (cajaActiva != null) {
            List<Venta> ventasDeCaja = ventaRepository.findByCaja(cajaActiva);
            BigDecimal totalVentas = ventasDeCaja.stream()
                    .map(Venta::getTotalNeto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            List<Gasto> gastosDeCaja = gastoRepository.findByCaja(cajaActiva);
            BigDecimal totalGastosAdmin = gastosDeCaja.stream()
                    .map(Gasto::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalComprasProveedores = compraRepository.sumarTotalComprasPorUsuarioYFecha(usuario.getId(), cajaActiva.getFechaApertura());
            if (totalComprasProveedores == null) {
                totalComprasProveedores = BigDecimal.ZERO;
            }

            BigDecimal totalEgresos = totalGastosAdmin.add(totalComprasProveedores);
            
            BigDecimal montoEsperado = cajaActiva.getMontoInicial().add(totalVentas).subtract(totalEgresos);
            
            model.addAttribute("totalVentas", totalVentas);
            model.addAttribute("totalGastos", totalEgresos); 
            model.addAttribute("montoEsperado", montoEsperado);
            model.addAttribute("cantidadVentas", ventasDeCaja.size());
        }
        
        model.addAttribute("caja", cajaActiva);
        
        return "caja/gestion";
    }

    @PostMapping("/abrir")
    public String abrirCaja(@RequestParam BigDecimal montoInicial, 
                            @AuthenticationPrincipal UserDetails userDetails, 
                            RedirectAttributes redirect) {
                                
        if (montoInicial == null || montoInicial.compareTo(BigDecimal.ZERO) < 0) {
            redirect.addFlashAttribute("error", "Error: El fondo de apertura no puede ser un número negativo o vacío.");
            return "redirect:/caja/estado";
        }

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        if (cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA").isPresent()) {
            redirect.addFlashAttribute("error", "Ya tienes una caja abierta.");
            return "redirect:/caja/estado";
        }

        Caja nuevaCaja = new Caja();
        nuevaCaja.setUsuario(usuario);
        nuevaCaja.setMontoInicial(montoInicial);
        nuevaCaja.setTotalVentas(BigDecimal.ZERO);
        nuevaCaja.setMontoFinalEsperado(BigDecimal.ZERO);
        nuevaCaja.setMontoFinalReal(BigDecimal.ZERO);
        nuevaCaja.setDiferencia(BigDecimal.ZERO);
        nuevaCaja.setFechaApertura(LocalDateTime.now());
        nuevaCaja.setEstado("ABIERTA");
        
        cajaRepository.save(nuevaCaja);
        redirect.addFlashAttribute("success", "Caja abierta. ¡Buenas ventas en Menestras Villa!");
        return "redirect:/pos";
    }

    @PostMapping("/cerrar")
    public String cerrarCaja(@RequestParam BigDecimal montoReal, 
                             @AuthenticationPrincipal UserDetails userDetails, 
                             RedirectAttributes redirect) {
                                 
        if (montoReal == null || montoReal.compareTo(BigDecimal.ZERO) < 0) {
            redirect.addFlashAttribute("error", "Error: El monto físico contado debe ser un número válido o cero.");
            return "redirect:/caja/estado";
        }

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Caja caja = cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA").orElseThrow();
        
        BigDecimal totalVentas = ventaRepository.findByCaja(caja).stream()
                .map(Venta::getTotalNeto).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalGastosAdmin = gastoRepository.findByCaja(caja).stream()
                .map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal totalComprasProveedores = compraRepository.sumarTotalComprasPorUsuarioYFecha(usuario.getId(), caja.getFechaApertura());
        if (totalComprasProveedores == null) {
            totalComprasProveedores = BigDecimal.ZERO;
        }

        BigDecimal totalEgresos = totalGastosAdmin.add(totalComprasProveedores);
        
        BigDecimal montoEsperado = caja.getMontoInicial().add(totalVentas).subtract(totalEgresos);
        BigDecimal diferencia = montoReal.subtract(montoEsperado);
        
        caja.setTotalVentas(totalVentas);
        caja.setMontoFinalEsperado(montoEsperado);
        caja.setMontoFinalReal(montoReal);
        caja.setDiferencia(diferencia);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado("CERRADA");
        
        cajaRepository.save(caja);
        
        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            redirect.addFlashAttribute("success", "Caja cerrada con CUADRE EXACTO.");
        } else if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            redirect.addFlashAttribute("error", "FALTANTE de S/ " + diferencia.abs() + ". Revise los gastos registrados.");
        } else {
            redirect.addFlashAttribute("warning", "SOBRANTE de S/ " + diferencia);
        }
        
        return "redirect:/dashboard";
    }
}