package com.finanzas.backend_gestor_finanzas.repository;

import com.finanzas.backend_gestor_finanzas.model.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
    List<Ingreso> findByUsuarioId(Long usuarioId);
    List<Ingreso> findByGrupoId(Long grupoId);
    List<Ingreso> findByFechaBetween(LocalDate inicio, LocalDate fin);
    
    // Método para obtener ingresos del usuario Y de sus grupos
    @Query("SELECT i FROM Ingreso i WHERE i.usuario.id = :usuarioId OR i.grupo.id IN :grupoIds")
    List<Ingreso> findByUsuarioIdOrGrupoIds(@Param("usuarioId") Long usuarioId, @Param("grupoIds") List<Long> grupoIds);
}
