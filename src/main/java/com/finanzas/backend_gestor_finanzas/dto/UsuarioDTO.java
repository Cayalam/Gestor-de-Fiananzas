package com.finanzas.backend_gestor_finanzas.dto;

public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String divisaPref;

    public UsuarioDTO(Long id, String nombre, String email, String divisaPref) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.divisaPref = divisaPref;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getDivisaPref() { return divisaPref; }
}
