package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/gastos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class GastoController {

    private final GastoRepository gastoRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarGastos(Model model) {
        model.addAttribute("gastos", gastoRepository.findAllByOrderByFechaPagoDesc());
        return "gastos/lista";
    }

    @PostMapping("/guardar")
    public String guardarGasto(@ModelAttribute Gasto gasto, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            cajaRepository.findFirstByUsuarioAndEstadoOrderByFechaAperturaDesc(usuario, "ABIERTA")
                .ifPresent(gasto::setCaja);
            
            gasto.setResponsable(usuario.getNombreCompleto());
            gastoRepository.save(gasto);
            redirect.addFlashAttribute("success", "Gasto registrado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/gastos";
    }

    @PostMapping("/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarGasto(@ModelAttribute Gasto gasto, RedirectAttributes redirect) {
        try {
            Gasto existente = gastoRepository.findById(gasto.getId()).orElseThrow();
            existente.setCategoria(gasto.getCategoria());
            existente.setDescripcion(gasto.getDescripcion());
            existente.setMonto(gasto.getMonto());
            existente.setFechaPago(gasto.getFechaPago());
            gastoRepository.save(existente);
            redirect.addFlashAttribute("success", "Registro corregido exitosamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al corregir el registro.");
        }
        return "redirect:/gastos";
    }
}