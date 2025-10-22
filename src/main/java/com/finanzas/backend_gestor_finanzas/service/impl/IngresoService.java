package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.Ingreso;
import com.finanzas.backend_gestor_finanzas.repository.IngresoRepository;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioGrupoRepository;
import com.finanzas.backend_gestor_finanzas.service.BaseCrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngresoService extends BaseCrudService<Ingreso, Long, IngresoRepository> {

    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public IngresoService(IngresoRepository ingresoRepository,
                         UsuarioGrupoRepository usuarioGrupoRepository) {
        super(ingresoRepository);
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Obtiene todos los ingresos del usuario autenticado
     * Incluye ingresos personales y de los grupos a los que pertenece
     */
    public List<Ingreso> getAllByUsuario(Long usuarioId) {
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
    public Ingreso update(Long id, Ingreso entity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setMonto(entity.getMonto());
                    existing.setFecha(entity.getFecha());
                    existing.setDescripcion(entity.getDescripcion());
                    existing.setCategoria(entity.getCategoria());
                    existing.setBolsillo(entity.getBolsillo());
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
    }
}
