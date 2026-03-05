package com.menestrasvilla.gestion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreoRecuperacion(String destinatario, String token) {
        String enlaceRecuperacion = "http://localhost:9090/recuperar-password?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("sistema.rcontrasena9@gmail.com");
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de Contraseña - Menestras Villa");
        mensaje.setText("Hola,\n\n" +
                "Has solicitado restablecer tu contraseña en el sistema Menestras Villa.\n" +
                "Por favor, haz clic en el siguiente enlace para crear una nueva contraseña. Este enlace es válido por 15 minutos:\n\n" +
                enlaceRecuperacion + "\n\n" +
                "Si no solicitaste este cambio, ignora este correo.\n\n" +
                "Atentamente,\n" +
                "El equipo de Menestras Villa Sede Lima");

        mailSender.send(mensaje);
    }
}