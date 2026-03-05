package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.repository.UsuarioRepository;
import com.menestrasvilla.gestion.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/olvide-password")
    public String mostrarFormularioOlvido() {
        return "olvide-password";
    }

    @PostMapping("/olvide-password")
    public String procesarOlvido(@RequestParam("email") String email, RedirectAttributes redirect) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String token = UUID.randomUUID().toString(); 
            
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);
            
            emailService.enviarCorreoRecuperacion(usuario.getEmail(), token);
        }
        
        redirect.addFlashAttribute("success", "Si el correo existe en nuestra base de datos, hemos enviado un enlace de recuperación.");
        return "redirect:/login";
    }

    @GetMapping("/recuperar-password")
    public String mostrarFormularioRecuperar(@RequestParam("token") String token, Model model, RedirectAttributes redirect) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);
        
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getTokenExpiration().isBefore(LocalDateTime.now())) {
            redirect.addFlashAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
            return "redirect:/login";
        }
        
        model.addAttribute("token", token);
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String procesarNuevaPassword(@RequestParam("token") String token, 
                                        @RequestParam("password") String password, 
                                        RedirectAttributes redirect) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);
        
        if (usuarioOpt.isPresent() && usuarioOpt.get().getTokenExpiration().isAfter(LocalDateTime.now())) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setResetToken(null);
            usuario.setTokenExpiration(null);
            usuarioRepository.save(usuario);
            
            redirect.addFlashAttribute("success", "¡Tu contraseña ha sido actualizada exitosamente! Ya puedes iniciar sesión.");
        } else {
            redirect.addFlashAttribute("error", "Error al restablecer la contraseña.");
        }
        return "redirect:/login";
    }
}