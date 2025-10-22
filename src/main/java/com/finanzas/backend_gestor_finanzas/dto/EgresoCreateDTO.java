package com.finanzas.backend_gestor_finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EgresoCreateDTO {
    private Long usuarioId;
    private Long grupoId;
    private Long categoriaId;
    private Long bolsilloId;
    private BigDecimal monto;
    private LocalDate fecha;
    private String descripcion;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public Long getBolsilloId() { return bolsilloId; }
    public void setBolsilloId(Long bolsilloId) { this.bolsilloId = bolsilloId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
