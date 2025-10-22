package com.finanzas.backend_gestor_finanzas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bolsillo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "id_usuario"}, name = "uk_bolsillo_nombre_usuario"),
    @UniqueConstraint(columnNames = {"nombre", "id_grupo"}, name = "uk_bolsillo_nombre_grupo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bolsillo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bolsillo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // opcional si pertenece a un usuario personal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    private Grupo grupo; // opcional si es de un grupo

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(precision = 15, scale = 2)
    private BigDecimal saldo;
}
