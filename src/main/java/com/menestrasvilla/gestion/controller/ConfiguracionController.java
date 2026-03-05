package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.Configuracion;
import com.menestrasvilla.gestion.repository.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracion")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionRepository configuracionRepository;

    @GetMapping
    public String mostrarConfiguracion(Model model) {
        Configuracion config = configuracionRepository.findById(1L).orElse(new Configuracion());
        
        if (config.getPorcentajeIgv() == null) {
            config.setPorcentajeIgv(new java.math.BigDecimal("18.00"));
            config.setSimboloMoneda("S/");
        }

        model.addAttribute("config", config);
        return "configuracion/gestion";
    }

    @PostMapping("/guardar")
    public String guardarConfiguracion(@ModelAttribute Configuracion config, RedirectAttributes redirect) {
        config.setId(1L); 
        configuracionRepository.save(config);
        
        redirect.addFlashAttribute("success", "Los datos de la empresa se han actualizado correctamente.");
        return "redirect:/configuracion";
    }
}