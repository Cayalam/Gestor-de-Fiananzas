package com.finanzas.backend_gestor_finanzas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔹 Permitir orígenes del frontend (development y production)
        // Usar patrones específicos cuando allowCredentials = true
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        // En producción, agregar: "https://tu-dominio.com"

        // 🔹 Permitir métodos HTTP comunes
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 🔹 Permitir cualquier header (necesario para Authorization)
        configuration.setAllowedHeaders(List.of("*"));

        // 🔹 Exponer encabezados personalizados al frontend
        configuration.setExposedHeaders(List.of("Authorization"));

        // 🔹 Permitir credenciales (cookies, Authorization, JWT, etc.)
        configuration.setAllowCredentials(true);

        // 🔹 Tiempo de cache para preflight requests (10 minutos)
        configuration.setMaxAge(3600L);

        // 🔹 Aplicar configuración global
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
