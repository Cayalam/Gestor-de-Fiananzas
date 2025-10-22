package com.finanzas.backend_gestor_finanzas.dto;

public class CategoriaDTO {
    private Long id;
    private Long usuarioId;  // 🔹 Agregado para que el frontend pueda filtrar
    private String nombre;
    private String tipo;
    private String usuario;
    private String grupo;

    public CategoriaDTO(Long id, Long usuarioId, String nombre, String tipo, String usuario, String grupo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.tipo = tipo;
        this.usuario = usuario;
        this.grupo = grupo;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getUsuario() { return usuario; }
    public String getGrupo() { return grupo; }
}
