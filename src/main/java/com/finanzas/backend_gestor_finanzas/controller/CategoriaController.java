package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Categoria;
import com.finanzas.backend_gestor_finanzas.service.impl.CategoriaService;
import com.finanzas.backend_gestor_finanzas.dto.CategoriaDTO;
import com.finanzas.backend_gestor_finanzas.dto.CategoriaCreateDTO;
import com.finanzas.backend_gestor_finanzas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<CategoriaDTO> create(@RequestBody CategoriaCreateDTO dto){
        // 🔒 SEGURIDAD: Usar siempre el usuario autenticado
        
        // Convertir el tipo del frontend al enum del backend
        Categoria.TipoCategoria tipoEnum;
        if ("ingreso".equalsIgnoreCase(dto.getTipo())) {
            tipoEnum = Categoria.TipoCategoria.ing;
        } else if ("gasto".equalsIgnoreCase(dto.getTipo())) {
            tipoEnum = Categoria.TipoCategoria.eg;
        } else {
            // Si no es válido, intentar parsear directamente (por si viene "ing" o "eg")
            try {
                tipoEnum = Categoria.TipoCategoria.valueOf(dto.getTipo());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        Categoria categoria = Categoria.builder()
            .nombre(dto.getNombre())
            .tipo(tipoEnum)
            .usuario(securityUtils.getCurrentUser())
            .build();
        
        Categoria creada = categoriaService.create(categoria);
        
        CategoriaDTO resp = new CategoriaDTO(
            creada.getId(),
            creada.getUsuario() != null ? creada.getUsuario().getId() : null,
            creada.getNombre(),
            creada.getTipo() != null ? creada.getTipo().name() : null,
            creada.getUsuario() != null ? creada.getUsuario().getNombre() : null,
            creada.getGrupo() != null ? creada.getGrupo().getNombre() : null
        );
        
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public List<CategoriaDTO> list(){
        // 🔒 SEGURIDAD: Solo devolver categorías del usuario autenticado
        Long usuarioId = securityUtils.getCurrentUserId();
        return categoriaService.getAllByUsuario(usuarioId).stream()
            .map(c -> new CategoriaDTO(
                c.getId(),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                c.getNombre(),
                c.getTipo() != null ? c.getTipo().name() : null,
                c.getUsuario() != null ? c.getUsuario().getNombre() : null,
                c.getGrupo() != null ? c.getGrupo().getNombre() : null
            ))
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> get(@PathVariable Long id){
        return categoriaService.getById(id)
            .map(c -> ResponseEntity.ok(new CategoriaDTO(
                c.getId(),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                c.getNombre(),
                c.getTipo() != null ? c.getTipo().name() : null,
                c.getUsuario() != null ? c.getUsuario().getNombre() : null,
                c.getGrupo() != null ? c.getGrupo().getNombre() : null
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> update(@PathVariable Long id, @RequestBody CategoriaCreateDTO dto){
        // Convertir el tipo del frontend al enum del backend
        Categoria.TipoCategoria tipoEnum;
        if ("ingreso".equalsIgnoreCase(dto.getTipo())) {
            tipoEnum = Categoria.TipoCategoria.ing;
        } else if ("gasto".equalsIgnoreCase(dto.getTipo())) {
            tipoEnum = Categoria.TipoCategoria.eg;
        } else {
            // Si no es válido, intentar parsear directamente (por si viene "ing" o "eg")
            try {
                tipoEnum = Categoria.TipoCategoria.valueOf(dto.getTipo());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        Categoria categoria = Categoria.builder()
            .nombre(dto.getNombre())
            .tipo(tipoEnum)
            .build();
        
        Categoria updated = categoriaService.update(id, categoria);
        
        CategoriaDTO responseDto = new CategoriaDTO(
            updated.getId(),
            updated.getUsuario() != null ? updated.getUsuario().getId() : null,
            updated.getNombre(),
            updated.getTipo() != null ? updated.getTipo().name() : null,
            updated.getUsuario() != null ? updated.getUsuario().getNombre() : null,
            updated.getGrupo() != null ? updated.getGrupo().getNombre() : null
        );
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
