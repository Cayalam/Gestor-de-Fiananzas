package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.Categoria;
import com.finanzas.backend_gestor_finanzas.repository.CategoriaRepository;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioGrupoRepository;
import com.finanzas.backend_gestor_finanzas.service.BaseCrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CategoriaService extends BaseCrudService<Categoria, Long, CategoriaRepository> {

    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, 
                           UsuarioGrupoRepository usuarioGrupoRepository) {
        super(categoriaRepository);
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Obtiene todas las categorías del usuario autenticado
     * Incluye categorías personales y de los grupos a los que pertenece
     */
    public List<Categoria> getAllByUsuario(Long usuarioId) {
        // Obtener los IDs de los grupos del usuario
        List<Long> grupoIds = usuarioGrupoRepository.findByIdUsuarioId(usuarioId)
            .stream()
            .map(ug -> ug.getGrupo().getId())
            .collect(Collectors.toList());
        
        if (grupoIds.isEmpty()) {
            // Si no tiene grupos, solo devolver categorías personales
            return repository.findByUsuarioId(usuarioId);
        }
        
        // Devolver categorías del usuario Y de sus grupos
        return repository.findByUsuarioIdOrGrupoIds(usuarioId, grupoIds);
    }

    @Override
    public Categoria update(Long id, Categoria entity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNombre(entity.getNombre());
                    existing.setTipo(entity.getTipo());
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
    }
}
