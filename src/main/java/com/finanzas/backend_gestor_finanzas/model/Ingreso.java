package com.finanzas.backend_gestor_finanzas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ingreso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingreso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    private Grupo grupo; // opcional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bolsillo")
    private Bolsillo bolsillo;

    @Column(precision = 15, scale = 2)
    private BigDecimal monto;

    private LocalDate fecha;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
