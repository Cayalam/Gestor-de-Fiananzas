package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupo;
import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupoId;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioGrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioGrupoService {

    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public UsuarioGrupo create(UsuarioGrupo ug){
        return usuarioGrupoRepository.save(ug);
    }

    public Optional<UsuarioGrupo> getById(Long usuarioId, Long grupoId){
        return usuarioGrupoRepository.findById(new UsuarioGrupoId(usuarioId, grupoId));
    }

    public List<UsuarioGrupo> byUsuario(Long usuarioId){
        return usuarioGrupoRepository.findByIdUsuarioId(usuarioId);
    }

    public List<UsuarioGrupo> byGrupo(Long grupoId){
        return usuarioGrupoRepository.findByIdGrupoId(grupoId);
    }

    public void delete(Long usuarioId, Long grupoId){
        usuarioGrupoRepository.deleteById(new UsuarioGrupoId(usuarioId, grupoId));
    }
}
