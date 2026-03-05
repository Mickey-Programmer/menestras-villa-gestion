package com.menestrasvilla.gestion.config;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol("ADMIN"));
            rolRepository.save(new Rol("VENDEDOR"));
        }

        if (usuarioRepository.count() == 0) {
            Rol adminRol = rolRepository.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombreCompleto("Administrador Menestras Villa");
            admin.setEmail("luismiguelvillalon35@gmail.com");
            admin.setRol(adminRol);
            admin.setActivo(true);

            usuarioRepository.save(admin);
        }

        if (usuarioRepository.findByUsername("ventas_lima").isEmpty()) {
            Rol ventasRol = rolRepository.findByNombre("VENDEDOR")
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));

            Usuario ventas = new Usuario();
            ventas.setUsername("ventas_lima");
            ventas.setPassword(passwordEncoder.encode("lima2026")); 
            ventas.setNombreCompleto("Ventas Sede Lima");
            ventas.setEmail("miguelon910@gmail.com");
            ventas.setRol(ventasRol);
            ventas.setActivo(true);

            usuarioRepository.save(ventas);
        }
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categorias("Legumbres", true));
            categoriaRepository.save(new Categorias("Cereales", true));
            categoriaRepository.save(new Categorias("Harinas", true));
        }
    }
}