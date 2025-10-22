package com.finanzas.backend_gestor_finanzas.dto;

public class CategoriaCreateDTO {
    private Long usuarioId;
    private Long grupoId;
    private String nombre;
    private String tipo;  // "ingreso" o "gasto" desde el frontend

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
