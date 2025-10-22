package com.finanzas.backend_gestor_finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EgresoDTO {
    private Long id;
    private Long usuarioId;  // 🔹 Agregado para que el frontend pueda filtrar
    private String usuario;
    private String grupo;
    private String categoria;
    private Long categoriaId;  // 🔹 Agregado para facilitar relaciones
    private String bolsillo;
    private Long bolsilloId;  // 🔹 Agregado para facilitar relaciones
    private BigDecimal monto;
    private LocalDate fecha;
    private String descripcion;

    public EgresoDTO(Long id, Long usuarioId, String usuario, String grupo, String categoria, Long categoriaId, String bolsillo, Long bolsilloId, BigDecimal monto, LocalDate fecha, String descripcion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.usuario = usuario;
        this.grupo = grupo;
        this.categoria = categoria;
        this.categoriaId = categoriaId;
        this.bolsillo = bolsillo;
        this.bolsilloId = bolsilloId;
        this.monto = monto;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getUsuario() { return usuario; }
    public String getGrupo() { return grupo; }
    public String getCategoria() { return categoria; }
    public Long getCategoriaId() { return categoriaId; }
    public String getBolsillo() { return bolsillo; }
    public Long getBolsilloId() { return bolsilloId; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
}
