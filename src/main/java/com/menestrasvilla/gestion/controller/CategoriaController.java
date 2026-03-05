package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.Categorias;
import com.menestrasvilla.gestion.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("nuevaCategoria", new Categorias());
        return "categorias";
    }

    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute Categorias categoria, RedirectAttributes redirect) {
        String nombreLimpio = categoria.getNombre().trim();
        categoria.setNombre(nombreLimpio);
        
        if (categoria.getId() == null && categoriaRepository.existsByNombre(nombreLimpio)) {
            redirect.addFlashAttribute("error", "Error: La categoría ya existe.");
            return "redirect:/categorias";
        }
        
        if (categoria.getId() == null) {
            categoria.setActivo(true);
        }

        categoriaRepository.save(categoria);
        redirect.addFlashAttribute("success", "Categoría gestionada con éxito.");
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("nuevaCategoria", categoriaRepository.findById(id).orElse(new Categorias()));
        return "categorias";
    }

    @GetMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirect) {
        Categorias cat = categoriaRepository.findById(id).orElseThrow();
        cat.setActivo(!cat.getActivo());
        categoriaRepository.save(cat);
        
        String accion = cat.getActivo() ? "habilitada" : "deshabilitada";
        redirect.addFlashAttribute("success", "Categoría " + accion + " correctamente.");
        return "redirect:/categorias";
    }
}