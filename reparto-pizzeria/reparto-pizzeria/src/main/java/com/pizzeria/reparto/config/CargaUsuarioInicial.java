package com.pizzeria.reparto.config;

import com.pizzeria.reparto.model.Usuario;
import com.pizzeria.reparto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CargaUsuarioInicial implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seguridad.usuario-inicial}")
    private String usernameInicial;

    @Value("${seguridad.password-inicial}")
    private String passwordInicial;

    @Value("${seguridad.email-inicial}")
    private String emailInicial;

    public CargaUsuarioInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByUsername(usernameInicial).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(usernameInicial);
        usuario.setEmail(emailInicial);
        usuario.setPassword(passwordEncoder.encode(passwordInicial));
        usuarioRepository.save(usuario);
    }
}