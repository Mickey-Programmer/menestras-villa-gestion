package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.MovimientoInventario;
import com.menestrasvilla.gestion.repository.MovimientoInventarioRepository;
import com.menestrasvilla.gestion.service.ReporteExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ReporteExcelService reporteExcelService; 

    @GetMapping("/movimientos")
    public String verHistorialMovimientos(Model model) {
        model.addAttribute("movimientos", movimientoRepository.findAllByOrderByFechaHoraDesc());
        return "inventario/movimientos";
    }

    @GetMapping("/movimientos/exportar")
    public void exportarKardexExcel(HttpServletResponse response) throws IOException {
        List<MovimientoInventario> movimientos = movimientoRepository.findAllByOrderByFechaHoraDesc();
        reporteExcelService.exportarKardex(movimientos, response);
    }
}