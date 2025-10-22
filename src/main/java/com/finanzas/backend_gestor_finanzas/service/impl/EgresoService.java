package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.Egreso;
import com.finanzas.backend_gestor_finanzas.repository.EgresoRepository;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioGrupoRepository;
import com.finanzas.backend_gestor_finanzas.service.BaseCrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EgresoService extends BaseCrudService<Egreso, Long, EgresoRepository> {

    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public EgresoService(EgresoRepository egresoRepository,
                        UsuarioGrupoRepository usuarioGrupoRepository) {
        super(egresoRepository);
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Obtiene todos los egresos del usuario autenticado
     * Incluye egresos personales y de los grupos a los que pertenece
     */
    public List<Egreso> getAllByUsuario(Long usuarioId) {
        // Obtener los IDs de los grupos del usuario
        List<Long> grupoIds = usuarioGrupoRepository.findByIdUsuarioId(usuarioId)
            .stream()
            .map(ug -> ug.getGrupo().getId())
            .collect(Collectors.toList());
        
        if (grupoIds.isEmpty()) {
            return repository.findByUsuarioId(usuarioId);
        }
        
        return repository.findByUsuarioIdOrGrupoIds(usuarioId, grupoIds);
    }

    @Override
    public Egreso update(Long id, Egreso entity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setMonto(entity.getMonto());
                    existing.setFecha(entity.getFecha());
                    existing.setDescripcion(entity.getDescripcion());
                    existing.setCategoria(entity.getCategoria());
                    existing.setBolsillo(entity.getBolsillo());
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Egreso no encontrado"));
    }
}
