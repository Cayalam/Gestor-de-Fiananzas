package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Usuario;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioRepository;
import com.finanzas.backend_gestor_finanzas.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // 1) Buscar usuario por email
        Usuario u = usuarioRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        // 2) Validar contraseña con PasswordEncoder
        if (!passwordEncoder.matches(req.getContrasena(), u.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        // 3) Generar JWT y responder
        String token = jwtUtil.generateToken(u.getEmail(), u.getId());
        AuthResponse body = new AuthResponse(token, u.getNombre(), u.getEmail(), u.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(body);
    }

    // =========================
    // REGISTER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // Validaciones mínimas
        if (req.getNombre() == null || req.getEmail() == null || req.getContrasena() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan campos obligatorios");
        }

        // Email duplicado
        if (usuarioRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        // Crear y guardar usuario (encripta contraseña)
        Usuario nuevo = new Usuario();
        nuevo.setNombre(req.getNombre());
        nuevo.setEmail(req.getEmail());
        nuevo.setContrasena(passwordEncoder.encode(req.getContrasena()));
        nuevo.setDivisaPref(req.getDivisaPref()); // puede venir null y no hay problema

        usuarioRepository.save(nuevo);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario creado correctamente");
    }

    // =========================
    // DTOs
    // =========================
    @Data
    static class LoginRequest {
        private String email;
        private String contrasena;
    }

    @Data
    static class RegisterRequest {
        private String nombre;
        private String email;
        private String contrasena;
        private String divisaPref; // COP / USD / EUR / MXN ...
    }

    @Data
    @AllArgsConstructor
    static class AuthResponse {
        private String token;
        private String nombre;
        private String email;
        private Long usuarioId;
    }
}
