package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Bolsillo;
import com.finanzas.backend_gestor_finanzas.service.impl.BolsilloService;
import com.finanzas.backend_gestor_finanzas.dto.BolsilloDTO;
import com.finanzas.backend_gestor_finanzas.dto.BolsilloCreateDTO;
import com.finanzas.backend_gestor_finanzas.dto.BolsilloUpdateDTO;
import com.finanzas.backend_gestor_finanzas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bolsillos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BolsilloController {

    private final BolsilloService bolsilloService;
    private final com.finanzas.backend_gestor_finanzas.service.impl.GrupoService grupoService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<BolsilloDTO> create(@RequestBody BolsilloCreateDTO dto){
        // 🔒 SEGURIDAD: Usar siempre el usuario autenticado
        
        if (dto.getGrupoId() == null) {
            // Si no es de grupo, crear bolsillo personal del usuario autenticado
            Bolsillo bolsillo = Bolsillo.builder()
                .nombre(dto.getNombre())
                .saldo(dto.getSaldo())
                .usuario(securityUtils.getCurrentUser())
                .build();
            
            Bolsillo creado = bolsilloService.create(bolsillo);
            BolsilloDTO resp = new BolsilloDTO(
                creado.getId(),
                creado.getUsuario() != null ? creado.getUsuario().getId() : null,
                creado.getUsuario() != null ? creado.getUsuario().getNombre() : null,
                null,
                creado.getNombre(),
                creado.getSaldo()
            );
            return ResponseEntity.ok(resp);
        }

        // Si es de grupo, validar que exista
        var grupoOpt = grupoService.getById(dto.getGrupoId());
        if (grupoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Bolsillo bolsillo = Bolsillo.builder()
            .nombre(dto.getNombre())
            .saldo(dto.getSaldo())
            .grupo(grupoOpt.get())
            .build();
        
        Bolsillo creado = bolsilloService.create(bolsillo);
        BolsilloDTO resp = new BolsilloDTO(
            creado.getId(),
            null,
            null,
            creado.getGrupo() != null ? creado.getGrupo().getNombre() : null,
            creado.getNombre(),
            creado.getSaldo()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public List<BolsilloDTO> list(){
        // 🔒 SEGURIDAD: Solo devolver bolsillos del usuario autenticado
        Long usuarioId = securityUtils.getCurrentUserId();
        return bolsilloService.getAllByUsuario(usuarioId).stream()
            .map(b -> new BolsilloDTO(
                b.getId(),
                b.getUsuario() != null ? b.getUsuario().getId() : null,
                b.getUsuario() != null ? b.getUsuario().getNombre() : null,
                b.getGrupo() != null ? b.getGrupo().getNombre() : null,
                b.getNombre(),
                b.getSaldo()
            ))
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BolsilloDTO> get(@PathVariable Long id){
        return bolsilloService.getById(id)
            .map(b -> ResponseEntity.ok(new BolsilloDTO(
                b.getId(),
                b.getUsuario() != null ? b.getUsuario().getId() : null,
                b.getUsuario() != null ? b.getUsuario().getNombre() : null,
                b.getGrupo() != null ? b.getGrupo().getNombre() : null,
                b.getNombre(),
                b.getSaldo()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BolsilloDTO> update(@PathVariable Long id, @RequestBody BolsilloUpdateDTO dto){
        // 🔒 SEGURIDAD: Verificar que el bolsillo pertenezca al usuario actual
        Long usuarioId = securityUtils.getCurrentUserId();
        
        var bolsilloOpt = bolsilloService.getById(id);
        if (bolsilloOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Bolsillo bolsillo = bolsilloOpt.get();
        
        // Verificar que el usuario tenga permisos sobre este bolsillo
        boolean tienePermiso = false;
        if (bolsillo.getUsuario() != null && bolsillo.getUsuario().getId().equals(usuarioId)) {
            tienePermiso = true;
        } else if (bolsillo.getGrupo() != null) {
            // Verificar si el usuario pertenece al grupo
            var perteneceAlGrupo = grupoService.isUserInGroup(usuarioId, bolsillo.getGrupo().getId());
            if (perteneceAlGrupo) {
                tienePermiso = true;
            }
        }
        
        if (!tienePermiso) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        // Actualizar solo nombre y saldo
        Bolsillo updated = bolsilloService.updateBolsillo(id, dto.getNombre(), dto.getSaldo());
        
        BolsilloDTO respDto = new BolsilloDTO(
            updated.getId(),
            updated.getUsuario() != null ? updated.getUsuario().getId() : null,
            updated.getUsuario() != null ? updated.getUsuario().getNombre() : null,
            updated.getGrupo() != null ? updated.getGrupo().getNombre() : null,
            updated.getNombre(),
            updated.getSaldo()
        );
        return ResponseEntity.ok(respDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        try {
            // 🔒 SEGURIDAD: Verificar que el bolsillo pertenezca al usuario actual
            Long usuarioId = securityUtils.getCurrentUserId();
            
            if (usuarioId == null) {
                System.out.println("❌ Usuario no autenticado");
                return ResponseEntity.status(401).build();
            }
            
            var bolsilloOpt = bolsilloService.getById(id);
            if (bolsilloOpt.isEmpty()) {
                System.out.println("❌ Bolsillo no encontrado: " + id);
                return ResponseEntity.notFound().build();
            }
            
            Bolsillo bolsillo = bolsilloOpt.get();
            System.out.println("🗑️ Intentando eliminar bolsillo ID: " + id);
            System.out.println("   Usuario propietario: " + (bolsillo.getUsuario() != null ? bolsillo.getUsuario().getId() : "null"));
            System.out.println("   Usuario actual: " + usuarioId);
            System.out.println("   Grupo: " + (bolsillo.getGrupo() != null ? bolsillo.getGrupo().getId() : "null"));
            
            // Verificar que el usuario tenga permisos sobre este bolsillo
            boolean tienePermiso = false;
            
            // Si es bolsillo personal
            if (bolsillo.getUsuario() != null && bolsillo.getUsuario().getId().equals(usuarioId)) {
                tienePermiso = true;
                System.out.println("✅ Permiso concedido: Bolsillo personal");
            } 
            // Si es bolsillo de grupo (verificar pertenencia al grupo)
            else if (bolsillo.getGrupo() != null) {
                try {
                    var perteneceAlGrupo = grupoService.isUserInGroup(usuarioId, bolsillo.getGrupo().getId());
                    if (perteneceAlGrupo) {
                        tienePermiso = true;
                        System.out.println("✅ Permiso concedido: Usuario pertenece al grupo");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Error al verificar pertenencia al grupo: " + e.getMessage());
                }
            }
            
            if (!tienePermiso) {
                System.out.println("❌ Permiso denegado");
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            // Intentar eliminar el bolsillo
            try {
                bolsilloService.delete(id);
                System.out.println("✅ Bolsillo eliminado exitosamente");
                return ResponseEntity.noContent().build();
            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                // El bolsillo tiene transacciones asociadas
                System.out.println("❌ No se puede eliminar: El bolsillo tiene transacciones asociadas");
                return ResponseEntity.status(409) // Conflict
                    .body(null);
            }
            
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            System.out.println("❌ Violación de integridad referencial: " + ex.getMessage());
            return ResponseEntity.status(409).build(); // Conflict
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar bolsillo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
