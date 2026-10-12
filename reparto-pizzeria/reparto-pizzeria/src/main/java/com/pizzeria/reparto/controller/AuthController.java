package com.pizzeria.reparto.controller;

import com.pizzeria.reparto.model.PasswordResetToken;
import com.pizzeria.reparto.model.Usuario;
import com.pizzeria.reparto.repository.PasswordResetTokenRepository;
import com.pizzeria.reparto.repository.UsuarioRepository;
import com.pizzeria.reparto.security.JwtService;
import com.pizzeria.reparto.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int MINUTOS_VALIDEZ_TOKEN = 30;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String username) {}
    public record OlvidePasswordRequest(String email) {}
    public record RestablecerPasswordRequest(String token, String nuevaPassword) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest peticion) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(peticion.username(), peticion.password()));

        String token = jwtService.generarToken(peticion.username());
        return new LoginResponse(token, peticion.username());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> manejarCredencialesInvalidas() {
        return ResponseEntity.status(401).body("Usuario o contraseña incorrectos.");
    }

    @PostMapping("/olvide-password")
    public ResponseEntity<String> olvidePassword(@RequestBody OlvidePasswordRequest peticion) {
        usuarioRepository.findByEmail(peticion.email()).ifPresent(usuario -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUsuario(usuario);
            resetToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ_TOKEN));
            tokenRepository.save(resetToken);

            emailService.enviarCorreoRestablecimiento(usuario.getEmail(), resetToken.getToken());
        });

        return ResponseEntity.ok("Si ese email existe en el sistema, se ha enviado un correo con instrucciones.");
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<String> restablecerPassword(@RequestBody RestablecerPasswordRequest peticion) {
        PasswordResetToken resetToken = tokenRepository.findByToken(peticion.token())
                .orElse(null);

        if (resetToken == null || !resetToken.esValido()) {
            return ResponseEntity.badRequest().body("El enlace no es valido o ha caducado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(peticion.nuevaPassword()));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}