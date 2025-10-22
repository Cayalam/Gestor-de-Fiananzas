package com.finanzas.backend_gestor_finanzas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_grupo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioGrupo {
    @EmbeddedId
    private UsuarioGrupoId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("usuarioId")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("grupoId")
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    @Column(length = 50)
    private String rol; // Ej: ADMIN, MIEMBRO
}
