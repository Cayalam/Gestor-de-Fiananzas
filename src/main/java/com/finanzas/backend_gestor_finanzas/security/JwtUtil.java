package com.finanzas.backend_gestor_finanzas.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "clave_super_secreta_para_finanzasdb_2025_segura_de_32_bytes";
    private final long EXPIRATION = 1000L * 60 * 60 * 10; // 10 horas

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, Long usuarioId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .claim("usuarioId", usuarioId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("usuarioId", Long.class);
    }

    public boolean validateToken(String token, String expectedUsername) {
        final String username = getUsernameFromToken(token);
        return username.equals(expectedUsername) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
