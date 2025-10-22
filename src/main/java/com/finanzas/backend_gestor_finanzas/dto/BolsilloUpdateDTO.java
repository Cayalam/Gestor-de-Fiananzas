package com.finanzas.backend_gestor_finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para actualizar un bolsillo existente
 * Solo permite modificar nombre y saldo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BolsilloUpdateDTO {
    private String nombre;
    private BigDecimal saldo;
}
