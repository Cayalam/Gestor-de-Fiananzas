package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Egreso;
import com.finanzas.backend_gestor_finanzas.service.impl.EgresoService;
import com.finanzas.backend_gestor_finanzas.service.impl.CategoriaService;
import com.finanzas.backend_gestor_finanzas.service.impl.BolsilloService;
import com.finanzas.backend_gestor_finanzas.dto.EgresoDTO;
import com.finanzas.backend_gestor_finanzas.dto.EgresoCreateDTO;
import com.finanzas.backend_gestor_finanzas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/egresos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EgresoController {

    private final EgresoService egresoService;
    private final CategoriaService categoriaService;
    private final BolsilloService bolsilloService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<EgresoDTO> create(@RequestBody EgresoCreateDTO dto){
        // 🔒 SEGURIDAD: Usar siempre el usuario autenticado
        
        // Validar que la categoría y el bolsillo existen
        var categoria = categoriaService.getById(dto.getCategoriaId())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        var bolsillo = bolsilloService.getById(dto.getBolsilloId())
            .orElseThrow(() -> new RuntimeException("Bolsillo no encontrado"));
        
        Egreso egreso = Egreso.builder()
            .usuario(securityUtils.getCurrentUser())
            .categoria(categoria)
            .bolsillo(bolsillo)
            .monto(dto.getMonto())
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
            .descripcion(dto.getDescripcion())
            .build();
        
        Egreso creado = egresoService.create(egreso);
        
        EgresoDTO resp = new EgresoDTO(
            creado.getId(),
            creado.getUsuario() != null ? creado.getUsuario().getId() : null,
            creado.getUsuario() != null ? creado.getUsuario().getNombre() : null,
            creado.getGrupo() != null ? creado.getGrupo().getNombre() : null,
            creado.getCategoria() != null ? creado.getCategoria().getNombre() : null,
            creado.getCategoria() != null ? creado.getCategoria().getId() : null,
            creado.getBolsillo() != null ? creado.getBolsillo().getNombre() : null,
            creado.getBolsillo() != null ? creado.getBolsillo().getId() : null,
            creado.getMonto(),
            creado.getFecha(),
            creado.getDescripcion()
        );
        
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public List<EgresoDTO> list(){
        // 🔒 SEGURIDAD: Solo devolver egresos del usuario autenticado
        Long usuarioId = securityUtils.getCurrentUserId();
        return egresoService.getAllByUsuario(usuarioId).stream()
            .map(e -> new EgresoDTO(
                e.getId(),
                e.getUsuario() != null ? e.getUsuario().getId() : null,
                e.getUsuario() != null ? e.getUsuario().getNombre() : null,
                e.getGrupo() != null ? e.getGrupo().getNombre() : null,
                e.getCategoria() != null ? e.getCategoria().getNombre() : null,
                e.getCategoria() != null ? e.getCategoria().getId() : null,
                e.getBolsillo() != null ? e.getBolsillo().getNombre() : null,
                e.getBolsillo() != null ? e.getBolsillo().getId() : null,
                e.getMonto(),
                e.getFecha(),
                e.getDescripcion()
            ))
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EgresoDTO> get(@PathVariable Long id){
        return egresoService.getById(id)
            .map(e -> ResponseEntity.ok(new EgresoDTO(
                e.getId(),
                e.getUsuario() != null ? e.getUsuario().getId() : null,
                e.getUsuario() != null ? e.getUsuario().getNombre() : null,
                e.getGrupo() != null ? e.getGrupo().getNombre() : null,
                e.getCategoria() != null ? e.getCategoria().getNombre() : null,
                e.getCategoria() != null ? e.getCategoria().getId() : null,
                e.getBolsillo() != null ? e.getBolsillo().getNombre() : null,
                e.getBolsillo() != null ? e.getBolsillo().getId() : null,
                e.getMonto(),
                e.getFecha(),
                e.getDescripcion()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgresoDTO> update(@PathVariable Long id, @RequestBody Egreso egreso){
        Egreso updated = egresoService.update(id, egreso);
        EgresoDTO dto = new EgresoDTO(
            updated.getId(),
            updated.getUsuario() != null ? updated.getUsuario().getId() : null,
            updated.getUsuario() != null ? updated.getUsuario().getNombre() : null,
            updated.getGrupo() != null ? updated.getGrupo().getNombre() : null,
            updated.getCategoria() != null ? updated.getCategoria().getNombre() : null,
            updated.getCategoria() != null ? updated.getCategoria().getId() : null,
            updated.getBolsillo() != null ? updated.getBolsillo().getNombre() : null,
            updated.getBolsillo() != null ? updated.getBolsillo().getId() : null,
            updated.getMonto(),
            updated.getFecha(),
            updated.getDescripcion()
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        egresoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
