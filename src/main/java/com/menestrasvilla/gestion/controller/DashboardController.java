package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.Caja;
import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GastoRepository gastoRepository; 

    @GetMapping("/dashboard")
    public String mostrarDashboard(@AuthenticationPrincipal UserDetails userDetails, 
                                   @RequestParam(value = "periodo", defaultValue = "mes") String periodo,
                                   Model model) {
        
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- LÓGICA DE FILTRADO POR PERIODO ---
        LocalDateTime fechaInicio;
        LocalDateTime fechaFin = LocalDateTime.now();

        switch (periodo) {
            case "hoy":
                fechaInicio = LocalDateTime.now().with(LocalTime.MIN);
                break;
            case "semana":
                fechaInicio = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
                break;
            case "siempre":
                fechaInicio = LocalDateTime.of(2000, 1, 1, 0, 0); 
                break;
            case "mes":
            default:
                fechaInicio = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
                periodo = "mes";
                break;
        }
        model.addAttribute("periodoActual", periodo);

        Optional<Caja> cajaActiva = cajaRepository.findFirstByUsuarioAndEstadoOrderByFechaAperturaDesc(usuario, "ABIERTA");
        BigDecimal ventasTurno = BigDecimal.ZERO;
        if (cajaActiva.isPresent()) {
            BigDecimal total = ventaRepository.sumarTotalNetoPorCaja(cajaActiva.get().getId());
            ventasTurno = total != null ? total : BigDecimal.ZERO;
        }
        model.addAttribute("ventasTurno", ventasTurno);
        
        model.addAttribute("alertasStock", productoRepository.contarProductosConBajoStock());
        model.addAttribute("productosBajoStock", productoRepository.findProductosConBajoStock());
        
        LocalDateTime hace7Dias = LocalDateTime.now().minusDays(7);
        model.addAttribute("cajasCerradas", cajaRepository.countByEstadoAndFechaCierreAfter("CERRADA", hace7Dias));

        BigDecimal totalIngresos = ventaRepository.sumarTotalNetoPorFechas(fechaInicio, fechaFin);
        totalIngresos = (totalIngresos != null) ? totalIngresos : BigDecimal.ZERO;
        model.addAttribute("totalIngresos", totalIngresos);

        BigDecimal totalEgresos = gastoRepository.sumarTotalGastosPorFechas(fechaInicio.toLocalDate(), fechaFin.toLocalDate());
        totalEgresos = (totalEgresos != null) ? totalEgresos : BigDecimal.ZERO;
        model.addAttribute("totalEgresos", totalEgresos);

        BigDecimal utilidadNeta = totalIngresos.subtract(totalEgresos);
        model.addAttribute("utilidadNeta", utilidadNeta);

        List<Object[]> comprobantesDB = ventaRepository.contarVentasPorTipoComprobantePorFechas(fechaInicio, fechaFin);
        model.addAttribute("comprobantesLabels", comprobantesDB.stream().map(f -> (String) f[0]).collect(Collectors.toList()));
        model.addAttribute("comprobantesData", comprobantesDB.stream().map(f -> (Long) f[1]).collect(Collectors.toList()));

        List<Object[]> productosTopDB = detalleVentaRepository.findProductosMasVendidosPorFechas(fechaInicio, fechaFin, PageRequest.of(0, 5));
        model.addAttribute("topProductosLabels", productosTopDB.stream().map(f -> (String) f[0]).collect(Collectors.toList()));
        model.addAttribute("topProductosData", productosTopDB.stream().map(f -> new BigDecimal(f[1].toString())).collect(Collectors.toList()));

        return "dashboard"; 
    }
    
    @GetMapping("/403")
    public String accesoDenegado() {
        return "error/403";
    }
}