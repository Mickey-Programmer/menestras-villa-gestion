package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.repository.RolRepository;
import com.menestrasvilla.gestion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("nuevoUsuario", new Usuario());
        return "usuarios/gestion";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, 
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirect) {
        try {
            if (usuario.getId() == null) {
                if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
                    redirect.addFlashAttribute("error", "Error: El nombre de usuario '" + usuario.getUsername() + "' ya existe.");
                    return "redirect:/usuarios";
                }
                
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
                redirect.addFlashAttribute("success", "Usuario creado con éxito.");
            } else {
                Usuario usuarioExistente = usuarioRepository.findById(usuario.getId()).orElseThrow();
                
                if (!usuarioExistente.getUsername().equals(usuario.getUsername()) && 
                    usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
                    redirect.addFlashAttribute("error", "Error: El nuevo nombre de usuario ya está en uso.");
                    return "redirect:/usuarios";
                }

                usuarioExistente.setNombreCompleto(usuario.getNombreCompleto());
                usuarioExistente.setUsername(usuario.getUsername());
                usuarioExistente.setEmail(usuario.getEmail());
                usuarioExistente.setRol(usuario.getRol());

                if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
                    usuarioExistente.setPassword(passwordEncoder.encode(usuario.getPassword()));
                }
                
                usuarioRepository.save(usuarioExistente);
                redirect.addFlashAttribute("success", "Datos del usuario actualizados.");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Ocurrió un error inesperado al procesar la solicitud.");
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuarioAEditar = usuarioRepository.findById(id).orElse(new Usuario());
        usuarioAEditar.setPassword(""); 
        
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("nuevoUsuario", usuarioAEditar);
        return "usuarios/gestion";
    }

    @GetMapping("/estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable Long id, 
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        
            if (usuario.getUsername().equals(userDetails.getUsername())) {
                redirect.addFlashAttribute("error", "¡Operación bloqueada! No puedes desactivar tu propia cuenta administrativa.");
                return "redirect:/usuarios";
            }

            usuario.setActivo(!usuario.isActivo());
            usuarioRepository.save(usuario);
            
            String mensaje = usuario.isActivo() ? "Usuario desbloqueado correctamente." 
                                                : "El usuario ha sido bloqueado y no podrá acceder.";
            redirect.addFlashAttribute("success", mensaje);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al intentar cambiar el estado del acceso.");
        }
        return "redirect:/usuarios";
    }
}