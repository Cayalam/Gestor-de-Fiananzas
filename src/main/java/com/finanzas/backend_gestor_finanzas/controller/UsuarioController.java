package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Usuario;
import com.finanzas.backend_gestor_finanzas.service.impl.UsuarioService;
import com.finanzas.backend_gestor_finanzas.dto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ✅ Crear usuario
    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.create(usuario));
    }

    // ✅ Listar todos los usuarios
    @GetMapping
    public List<UsuarioDTO> list() {
        return usuarioService.getAll().stream()
                .map(u -> new UsuarioDTO(
                        u.getId(),
                        u.getNombre(),
                        u.getEmail(),
                        u.getDivisaPref()
                ))
                .toList();
    }

    // ✅ Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> get(@PathVariable Long id) {
        return usuarioService.getById(id)
                .map(u -> ResponseEntity.ok(new UsuarioDTO(
                        u.getId(),
                        u.getNombre(),
                        u.getEmail(),
                        u.getDivisaPref()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario updated = usuarioService.update(id, usuario);
        UsuarioDTO dto = new UsuarioDTO(
                updated.getId(),
                updated.getNombre(),
                updated.getEmail(),
                updated.getDivisaPref()
        );
        return ResponseEntity.ok(dto);
    }

    // ✅ Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ NUEVO: obtener usuario autenticado (por JWT)
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        var usuarioOpt = usuarioService.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var u = usuarioOpt.get();
        UsuarioDTO dto = new UsuarioDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getDivisaPref()
        );

        return ResponseEntity.ok(dto);
    }
}
