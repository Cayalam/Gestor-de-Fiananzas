package com.finanzas.backend_gestor_finanzas.dto;

import java.math.BigDecimal;

public class BolsilloDTO {
    private Long id;
    private Long usuarioId;  // 🔹 Agregado para que el frontend pueda filtrar
    private String usuario;
    private String grupo;
    private String nombre;
    private BigDecimal saldo;

    public BolsilloDTO(Long id, Long usuarioId, String usuario, String grupo, String nombre, BigDecimal saldo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.usuario = usuario;
        this.grupo = grupo;
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getUsuario() { return usuario; }
    public String getGrupo() { return grupo; }
    public String getNombre() { return nombre; }
    public BigDecimal getSaldo() { return saldo; }
}
