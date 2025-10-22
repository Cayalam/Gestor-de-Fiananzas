package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupo;
import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupoId;
import com.finanzas.backend_gestor_finanzas.service.impl.UsuarioGrupoService;
import com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoCreateDTO;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioRepository;
import com.finanzas.backend_gestor_finanzas.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario-grupo")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioGrupoController {

    private final UsuarioGrupoService usuarioGrupoService;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UsuarioGrupoCreateDTO dto){
    var usuarioOpt = usuarioRepository.findById(dto.getUsuarioId());
        var grupoOpt = grupoRepository.findById(dto.getGrupoId());
        if (usuarioOpt.isEmpty() || grupoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario o grupo no existe");
        }
        UsuarioGrupo ug = UsuarioGrupo.builder()
            .id(new UsuarioGrupoId(dto.getUsuarioId(), dto.getGrupoId()))
            .usuario(usuarioOpt.get())
            .grupo(grupoOpt.get())
            .rol(dto.getRol())
            .build();
        UsuarioGrupo creado = usuarioGrupoService.create(ug);
        var response = new com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO(
            creado.getId().getUsuarioId(),
            creado.getId().getGrupoId(),
            creado.getRol()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO> byUsuario(@PathVariable Long usuarioId){
        return usuarioGrupoService.byUsuario(usuarioId)
            .stream()
            .map(ug -> new com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO(
                ug.getId().getUsuarioId(),
                ug.getId().getGrupoId(),
                ug.getRol()
            ))
            .toList();
    }

    @GetMapping("/grupo/{grupoId}")
    public List<com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO> byGrupo(@PathVariable Long grupoId){
        return usuarioGrupoService.byGrupo(grupoId)
            .stream()
            .map(ug -> new com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO(
                ug.getId().getUsuarioId(),
                ug.getId().getGrupoId(),
                ug.getRol()
            ))
            .toList();
    }

    @GetMapping("/{usuarioId}/{grupoId}")
    public ResponseEntity<com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO> get(@PathVariable Long usuarioId, @PathVariable Long grupoId){
        return usuarioGrupoService.getById(usuarioId, grupoId)
            .map(ug -> ResponseEntity.ok(new com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO(
                ug.getId().getUsuarioId(),
                ug.getId().getGrupoId(),
                ug.getRol()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{usuarioId}/{grupoId}")
    public ResponseEntity<Void> delete(@PathVariable Long usuarioId, @PathVariable Long grupoId){
        usuarioGrupoService.delete(usuarioId, grupoId);
        return ResponseEntity.noContent().build();
    }
}
