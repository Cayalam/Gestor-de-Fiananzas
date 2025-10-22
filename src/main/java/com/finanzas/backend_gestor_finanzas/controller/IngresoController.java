package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Ingreso;
import com.finanzas.backend_gestor_finanzas.service.impl.IngresoService;
import com.finanzas.backend_gestor_finanzas.service.impl.CategoriaService;
import com.finanzas.backend_gestor_finanzas.service.impl.BolsilloService;
import com.finanzas.backend_gestor_finanzas.dto.IngresoDTO;
import com.finanzas.backend_gestor_finanzas.dto.IngresoCreateDTO;
import com.finanzas.backend_gestor_finanzas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IngresoController {

    private final IngresoService ingresoService;
    private final CategoriaService categoriaService;
    private final BolsilloService bolsilloService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<IngresoDTO> create(@RequestBody IngresoCreateDTO dto){
        // 🔒 SEGURIDAD: Usar siempre el usuario autenticado
        
        // Validar que la categoría y el bolsillo existen
        var categoria = categoriaService.getById(dto.getCategoriaId())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        var bolsillo = bolsilloService.getById(dto.getBolsilloId())
            .orElseThrow(() -> new RuntimeException("Bolsillo no encontrado"));
        
        Ingreso ingreso = Ingreso.builder()
            .usuario(securityUtils.getCurrentUser())
            .categoria(categoria)
            .bolsillo(bolsillo)
            .monto(dto.getMonto())
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
            .descripcion(dto.getDescripcion())
            .build();
        
        Ingreso creado = ingresoService.create(ingreso);
        
        IngresoDTO resp = new IngresoDTO(
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
    public List<IngresoDTO> list(){
        // 🔒 SEGURIDAD: Solo devolver ingresos del usuario autenticado
        Long usuarioId = securityUtils.getCurrentUserId();
        return ingresoService.getAllByUsuario(usuarioId).stream()
            .map(i -> new IngresoDTO(
                i.getId(),
                i.getUsuario() != null ? i.getUsuario().getId() : null,
                i.getUsuario() != null ? i.getUsuario().getNombre() : null,
                i.getGrupo() != null ? i.getGrupo().getNombre() : null,
                i.getCategoria() != null ? i.getCategoria().getNombre() : null,
                i.getCategoria() != null ? i.getCategoria().getId() : null,
                i.getBolsillo() != null ? i.getBolsillo().getNombre() : null,
                i.getBolsillo() != null ? i.getBolsillo().getId() : null,
                i.getMonto(),
                i.getFecha(),
                i.getDescripcion()
            ))
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngresoDTO> get(@PathVariable Long id){
        return ingresoService.getById(id)
            .map(i -> ResponseEntity.ok(new IngresoDTO(
                i.getId(),
                i.getUsuario() != null ? i.getUsuario().getId() : null,
                i.getUsuario() != null ? i.getUsuario().getNombre() : null,
                i.getGrupo() != null ? i.getGrupo().getNombre() : null,
                i.getCategoria() != null ? i.getCategoria().getNombre() : null,
                i.getCategoria() != null ? i.getCategoria().getId() : null,
                i.getBolsillo() != null ? i.getBolsillo().getNombre() : null,
                i.getBolsillo() != null ? i.getBolsillo().getId() : null,
                i.getMonto(),
                i.getFecha(),
                i.getDescripcion()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngresoDTO> update(@PathVariable Long id, @RequestBody Ingreso ingreso){
        Ingreso updated = ingresoService.update(id, ingreso);
        IngresoDTO dto = new IngresoDTO(
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
        ingresoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
