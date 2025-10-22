package com.finanzas.backend_gestor_finanzas.repository;

import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupo;
import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioGrupoRepository extends JpaRepository<UsuarioGrupo, UsuarioGrupoId> {
    List<UsuarioGrupo> findByIdUsuarioId(Long usuarioId);
    List<UsuarioGrupo> findByIdGrupoId(Long grupoId);
}
