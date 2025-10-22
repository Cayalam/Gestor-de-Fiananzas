package com.finanzas.backend_gestor_finanzas.dto;

public class UsuarioGrupoResponseDTO {
    private Long usuarioId;
    private Long grupoId;
    private String rol;

    public UsuarioGrupoResponseDTO() {}

    public UsuarioGrupoResponseDTO(Long usuarioId, Long grupoId, String rol) {
        this.usuarioId = usuarioId;
        this.grupoId = grupoId;
        this.rol = rol;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
