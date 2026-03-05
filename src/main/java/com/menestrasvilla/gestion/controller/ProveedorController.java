package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.Proveedor;
import com.menestrasvilla.gestion.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
@PreAuthorize("hasAuthority('ADMIN')")
public class ProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @GetMapping
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("nuevoProveedor", new Proveedor());
        return "proveedores";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(@ModelAttribute Proveedor proveedor) {
        proveedorRepository.save(proveedor);
        return "redirect:/proveedores";
    }

    // NUEVO MÉTODO PARA EDITAR
    @GetMapping("/editar/{id}")
    public String editarProveedor(@PathVariable Long id, Model model) {
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("nuevoProveedor", proveedorRepository.findById(id).orElse(new Proveedor()));
        return "proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id) {
        try {
            proveedorRepository.deleteById(id);
        } catch (Exception e) {
        }
        return "redirect:/proveedores";
    }
}