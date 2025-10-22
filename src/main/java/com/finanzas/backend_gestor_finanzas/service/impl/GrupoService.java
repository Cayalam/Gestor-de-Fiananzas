package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.Grupo;
import com.finanzas.backend_gestor_finanzas.repository.GrupoRepository;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioGrupoRepository;
import com.finanzas.backend_gestor_finanzas.service.BaseCrudService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GrupoService extends BaseCrudService<Grupo, Long, GrupoRepository> {

    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public GrupoService(GrupoRepository grupoRepository,
                       UsuarioGrupoRepository usuarioGrupoRepository) {
        super(grupoRepository);
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Obtiene todos los grupos a los que pertenece el usuario autenticado
     */
    public List<Grupo> getAllByUsuario(Long usuarioId) {
        System.out.println("🔎 GrupoService: Buscando relaciones usuario-grupo para usuarioId=" + usuarioId);
        var relaciones = usuarioGrupoRepository.findByIdUsuarioId(usuarioId);
        System.out.println("🔗 Relaciones encontradas: " + relaciones.size());
        
        relaciones.forEach(rel -> System.out.println("   - UsuarioGrupo: Usuario=" + rel.getId().getUsuarioId() + 
                                                     ", Grupo=" + rel.getId().getGrupoId() + ", Rol=" + rel.getRol()));
        
        return relaciones.stream()
            .map(ug -> {
                Grupo grupo = ug.getGrupo();
                System.out.println("   → Grupo: ID=" + grupo.getId() + ", Nombre=" + grupo.getNombre());
                return grupo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Grupo create(Grupo entity) {
        if (entity.getFechaCreacion() == null) {
            entity.setFechaCreacion(LocalDate.now());
        }
        return repository.save(entity);
    }

    @Override
    public Optional<Grupo> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Grupo> getAll() {
        return repository.findAll();
    }

    @Override
    public Grupo update(Long id, Grupo entity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNombre(entity.getNombre());
                    existing.setDescripcion(entity.getDescripcion());
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Verifica si un usuario pertenece a un grupo
     */
    public boolean isUserInGroup(Long usuarioId, Long grupoId) {
        return usuarioGrupoRepository.findByIdUsuarioId(usuarioId)
            .stream()
            .anyMatch(ug -> ug.getGrupo().getId().equals(grupoId));
    }
}
