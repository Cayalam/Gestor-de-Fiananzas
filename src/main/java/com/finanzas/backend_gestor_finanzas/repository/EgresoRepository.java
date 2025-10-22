package com.finanzas.backend_gestor_finanzas.repository;

import com.finanzas.backend_gestor_finanzas.model.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EgresoRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findByUsuarioId(Long usuarioId);
    List<Egreso> findByGrupoId(Long grupoId);
    List<Egreso> findByFechaBetween(LocalDate inicio, LocalDate fin);
    
    // Método para obtener egresos del usuario Y de sus grupos
    @Query("SELECT e FROM Egreso e WHERE e.usuario.id = :usuarioId OR e.grupo.id IN :grupoIds")
    List<Egreso> findByUsuarioIdOrGrupoIds(@Param("usuarioId") Long usuarioId, @Param("grupoIds") List<Long> grupoIds);
}
