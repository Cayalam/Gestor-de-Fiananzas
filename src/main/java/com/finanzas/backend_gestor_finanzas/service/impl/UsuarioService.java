package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.Usuario;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioRepository;
import com.finanzas.backend_gestor_finanzas.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements CrudService<Usuario, Long> {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario create(Usuario entity) {
        if (entity.getContrasena() != null && !entity.getContrasena().startsWith("$2a$")) {
            entity.setContrasena(passwordEncoder.encode(entity.getContrasena()));
        }
        return usuarioRepository.save(entity);
    }

    @Override
    public Optional<Usuario> getById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario update(Long id, Usuario entity) {
        return usuarioRepository.findById(id)
                .map(existing -> {
                    existing.setNombre(entity.getNombre());
                    existing.setEmail(entity.getEmail());
                    if (entity.getContrasena() != null) {
                        if (!entity.getContrasena().startsWith("$2a$")) {
                            existing.setContrasena(passwordEncoder.encode(entity.getContrasena()));
                        } else {
                            existing.setContrasena(entity.getContrasena());
                        }
                    }
                    existing.setDivisaPref(entity.getDivisaPref());
                    return usuarioRepository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }

    // ✅ Nuevo método para obtener usuario por email
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
