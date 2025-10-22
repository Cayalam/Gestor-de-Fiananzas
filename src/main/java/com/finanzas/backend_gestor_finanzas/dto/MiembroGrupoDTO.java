package com.finanzas.backend_gestor_finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un miembro de un grupo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiembroGrupoDTO {
    private Long usuarioId;
    private String nombre;
    private String email;
    private String rol;
}
