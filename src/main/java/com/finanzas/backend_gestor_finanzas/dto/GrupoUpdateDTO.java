package com.finanzas.backend_gestor_finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoUpdateDTO {
    private String nombre;
    private String descripcion;
    private List<String> nuevosMiembros; // Emails de los nuevos miembros a añadir
}
