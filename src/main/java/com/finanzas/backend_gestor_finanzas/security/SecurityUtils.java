package com.finanzas.backend_gestor_finanzas.security;

import com.finanzas.backend_gestor_finanzas.model.Usuario;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    public SecurityUtils(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene el usuario autenticado actual desde el contexto de seguridad
     * @return Usuario autenticado
     * @throws RuntimeException si no hay usuario autenticado
     */
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Obtiene el ID del usuario autenticado actual
     * @return ID del usuario
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Obtiene el email del usuario autenticado actual
     * @return Email del usuario
     */
    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }
}
