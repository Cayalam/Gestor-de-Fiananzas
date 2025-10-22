package com.finanzas.backend_gestor_finanzas.dto;

import java.time.LocalDate;
import java.util.List;

public class GrupoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaCreacion;
    private List<UsuarioGrupoResponseDTO> usuarios;

    public GrupoResponseDTO() {}

    public GrupoResponseDTO(Long id, String nombre, String descripcion, LocalDate fechaCreacion, List<UsuarioGrupoResponseDTO> usuarios) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.usuarios = usuarios;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<UsuarioGrupoResponseDTO> getUsuarios() { return usuarios; }
    public void setUsuarios(List<UsuarioGrupoResponseDTO> usuarios) { this.usuarios = usuarios; }
}
