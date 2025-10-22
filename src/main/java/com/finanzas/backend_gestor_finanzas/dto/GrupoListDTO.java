package com.finanzas.backend_gestor_finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO simplificado para listar grupos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoListDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaCreacion;
    private Integer cantidadMiembros;
    private List<MiembroGrupoDTO> miembros = new ArrayList<>();
}
