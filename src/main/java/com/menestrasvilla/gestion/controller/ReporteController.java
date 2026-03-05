package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.*;
import com.menestrasvilla.gestion.service.ReporteExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ReporteController {

    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final CajaRepository cajaRepository;
    private final GastoRepository gastoRepository;
    private final ReporteExcelService reporteExcelService;

    @GetMapping
    public String mostrarReportes(Model model) {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);

        List<Venta> ventasHoy = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicioDia, finDia);
        BigDecimal totalRecaudado = ventasHoy.stream().map(Venta::getTotalNeto).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("ventas", ventasHoy);
        model.addAttribute("totalRecaudado", totalRecaudado);
        model.addAttribute("fechaInicio", LocalDate.now().toString());
        model.addAttribute("fechaFin", LocalDate.now().toString());
        model.addAttribute("tipoComprobanteSeleccionado", "TODOS");
        model.addAttribute("tipoReporte", "VENTAS");

        return "ventas/reportes";
    }

    @GetMapping("/filtrar")
    public String filtrarReportes(@RequestParam String fechaInicio,
                                  @RequestParam String fechaFin,
                                  @RequestParam(required = false, defaultValue = "TODOS") String tipoComprobante,
                                  @RequestParam(required = false, defaultValue = "VENTAS") String tipoReporte,
                                  Model model) {
        
        LocalDateTime inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);

        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("tipoReporte", tipoReporte);

        if ("GASTOS".equals(tipoReporte)) {
            LocalDate inicioGasto = LocalDate.parse(fechaInicio);
            LocalDate finGasto = LocalDate.parse(fechaFin);
            
            List<Gasto> gastosFiltrados = gastoRepository.findByFechaPagoBetweenOrderByFechaPagoDesc(inicioGasto, finGasto);
            BigDecimal totalGastos = gastosFiltrados.stream().map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("gastos", gastosFiltrados);
            model.addAttribute("totalGastosReporte", totalGastos);

        } else if ("COMPRAS".equals(tipoReporte)) {
            List<Compra> comprasFiltradas = compraRepository.findByCreadoEnBetweenOrderByCreadoEnDesc(inicio, fin);
            BigDecimal totalInvertido = comprasFiltradas.stream().map(Compra::getTotalCompra).reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("compras", comprasFiltradas);
            model.addAttribute("totalInvertido", totalInvertido);
            
        } else if ("CAJAS".equals(tipoReporte)) {
            List<Caja> cajasFiltradas = cajaRepository.findByFechaCierreBetweenAndEstadoOrderByFechaCierreDesc(inicio, fin, "CERRADA");
            model.addAttribute("cajas", cajasFiltradas);
            
        } else {
            List<Venta> ventasFiltradas;
            if ("TODOS".equals(tipoComprobante)) {
                ventasFiltradas = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
            } else {
                ventasFiltradas = ventaRepository.findByFechaBetweenAndTipoComprobanteOrderByFechaDesc(inicio, fin, tipoComprobante);
            }
            BigDecimal totalRecaudado = ventasFiltradas.stream().map(Venta::getTotalNeto).reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("ventas", ventasFiltradas);
            model.addAttribute("totalRecaudado", totalRecaudado);
            model.addAttribute("tipoComprobanteSeleccionado", tipoComprobante);
        }

        return "ventas/reportes";
    }

    @GetMapping("/excel")
    public void descargarExcel(@RequestParam String fechaInicio,
                               @RequestParam String fechaFin,
                               @RequestParam(required = false, defaultValue = "TODOS") String tipoComprobante,
                               @RequestParam(required = false, defaultValue = "VENTAS") String tipoReporte,
                               HttpServletResponse response) throws IOException {
        
        LocalDateTime inicio = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);

        if ("GASTOS".equals(tipoReporte)) {
            LocalDate inicioGasto = LocalDate.parse(fechaInicio);
            LocalDate finGasto = LocalDate.parse(fechaFin);
            
            List<Gasto> gastos = gastoRepository.findByFechaPagoBetweenOrderByFechaPagoDesc(inicioGasto, finGasto);
            reporteExcelService.exportarGastos(gastos, response);
            
        } else if ("COMPRAS".equals(tipoReporte)) {
            List<Compra> compras = compraRepository.findByCreadoEnBetweenOrderByCreadoEnDesc(inicio, fin);
            reporteExcelService.exportarCompras(compras, response);
            
        } else if ("CAJAS".equals(tipoReporte)) {
            List<Caja> cajas = cajaRepository.findByFechaCierreBetweenAndEstadoOrderByFechaCierreDesc(inicio, fin, "CERRADA");
            reporteExcelService.exportarCajas(cajas, response);
            
        } else {
            List<Venta> ventas = ("TODOS".equals(tipoComprobante)) 
                ? ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                : ventaRepository.findByFechaBetweenAndTipoComprobanteOrderByFechaDesc(inicio, fin, tipoComprobante);
            reporteExcelService.exportarVentas(ventas, response);
        }
    }
}