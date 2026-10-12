package com.pizzeria.reparto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoRestablecimiento(String destinatario, String token) {
        String enlace = frontendUrl + "/restablecer-password?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Restablecer tu contraseña - Panel de reparto");
        mensaje.setText(
                "Has pedido restablecer tu contraseña.\n\n" +
                        "Entra en este enlace para elegir una nueva (caduca en 30 minutos):\n" +
                        enlace + "\n\n" +
                        "Si no has sido tú, puedes ignorar este correo."
        );

        mailSender.send(mensaje);
    }
}