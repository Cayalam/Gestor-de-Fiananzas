package com.finanzas.backend_gestor_finanzas.dto;

public class UsuarioGrupoCreateDTO {
    private Long usuarioId;
    private Long grupoId;
    private String rol;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
