package com.finanzas.backend_gestor_finanzas.service.impl;

import com.finanzas.backend_gestor_finanzas.model.Bolsillo;
import com.finanzas.backend_gestor_finanzas.repository.BolsilloRepository;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioGrupoRepository;
import com.finanzas.backend_gestor_finanzas.service.BaseCrudService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class BolsilloService extends BaseCrudService<Bolsillo, Long, BolsilloRepository> {

    private final UsuarioGrupoRepository usuarioGrupoRepository;

    public BolsilloService(BolsilloRepository bolsilloRepository,
                          UsuarioGrupoRepository usuarioGrupoRepository) {
        super(bolsilloRepository);
        this.usuarioGrupoRepository = usuarioGrupoRepository;
    }

    /**
     * Obtiene todos los bolsillos del usuario autenticado
     * Incluye bolsillos personales y de los grupos a los que pertenece
     */
    public List<Bolsillo> getAllByUsuario(Long usuarioId) {
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
    public Bolsillo update(Long id, Bolsillo entity) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNombre(entity.getNombre());
                    existing.setSaldo(entity.getSaldo());
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Bolsillo no encontrado"));
    }

    /**
     * Actualiza solo el nombre y saldo de un bolsillo
     * NO modifica la relación con usuario o grupo
     */
    public Bolsillo updateBolsillo(Long id, String nombre, BigDecimal saldo) {
        return repository.findById(id)
                .map(existing -> {
                    // Solo actualizar nombre y saldo
                    // NO tocar usuario ni grupo para evitar duplicados
                    if (nombre != null) {
                        existing.setNombre(nombre);
                    }
                    if (saldo != null) {
                        existing.setSaldo(saldo);
                    }
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Bolsillo no encontrado"));
    }
}
