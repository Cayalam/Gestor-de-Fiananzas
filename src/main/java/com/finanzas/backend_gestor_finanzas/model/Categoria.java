package com.finanzas.backend_gestor_finanzas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "tipo", "id_usuario"}, name = "uk_categoria_nombre_tipo_usuario"),
    @UniqueConstraint(columnNames = {"nombre", "tipo", "id_grupo"}, name = "uk_categoria_nombre_tipo_grupo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // si es categoría privada

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    private Grupo grupo; // si es categoría de grupo

    @Column(length = 100, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(length = 5, nullable = false)
    private TipoCategoria tipo; // ing o eg

    public enum TipoCategoria { ing, eg }
}
