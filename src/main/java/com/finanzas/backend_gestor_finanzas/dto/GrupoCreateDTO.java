package com.finanzas.backend_gestor_finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para crear un nuevo grupo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoCreateDTO {
    private String nombre;
    private String descripcion;
    private List<String> miembros; // Lista de emails de los miembros a añadir
}
