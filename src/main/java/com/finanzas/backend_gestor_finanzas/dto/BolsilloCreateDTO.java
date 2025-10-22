package com.finanzas.backend_gestor_finanzas.dto;

import java.math.BigDecimal;

public class BolsilloCreateDTO {
    private Long usuarioId;
    private Long grupoId;
    private String nombre;
    private BigDecimal saldo;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}
