package com.finanzas.backend_gestor_finanzas.repository;

import com.finanzas.backend_gestor_finanzas.model.Bolsillo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BolsilloRepository extends JpaRepository<Bolsillo, Long> {
    List<Bolsillo> findByUsuarioId(Long usuarioId);
    List<Bolsillo> findByGrupoId(Long grupoId);
    
    // Método para obtener bolsillos del usuario Y de sus grupos
    @Query("SELECT b FROM Bolsillo b WHERE b.usuario.id = :usuarioId OR b.grupo.id IN :grupoIds")
    List<Bolsillo> findByUsuarioIdOrGrupoIds(@Param("usuarioId") Long usuarioId, @Param("grupoIds") List<Long> grupoIds);
}
