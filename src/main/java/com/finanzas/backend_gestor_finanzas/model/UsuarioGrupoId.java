package com.finanzas.backend_gestor_finanzas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGrupoId implements Serializable {
    @Column(name = "id_usuario")
    private Long usuarioId;

    @Column(name = "id_grupo")
    private Long grupoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioGrupoId that = (UsuarioGrupoId) o;
        return usuarioId.equals(that.usuarioId) && grupoId.equals(that.grupoId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(usuarioId, grupoId);
    }
}
