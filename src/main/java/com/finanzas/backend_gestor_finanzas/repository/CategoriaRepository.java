package com.finanzas.backend_gestor_finanzas.repository;

import com.finanzas.backend_gestor_finanzas.model.Categoria;
import com.finanzas.backend_gestor_finanzas.model.Categoria.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioId(Long usuarioId);
    List<Categoria> findByGrupoId(Long grupoId);
    List<Categoria> findByTipo(TipoCategoria tipo);
    
    // Método para obtener categorías del usuario Y de sus grupos
    @Query("SELECT c FROM Categoria c WHERE c.usuario.id = :usuarioId OR c.grupo.id IN :grupoIds")
    List<Categoria> findByUsuarioIdOrGrupoIds(@Param("usuarioId") Long usuarioId, @Param("grupoIds") List<Long> grupoIds);
}
