package com.finanzas.backend_gestor_finanzas.controller;

import com.finanzas.backend_gestor_finanzas.model.Grupo;
import com.finanzas.backend_gestor_finanzas.model.Usuario;
import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupo;
import com.finanzas.backend_gestor_finanzas.model.UsuarioGrupoId;
import com.finanzas.backend_gestor_finanzas.dto.GrupoCreateDTO;
import com.finanzas.backend_gestor_finanzas.dto.GrupoUpdateDTO;
import com.finanzas.backend_gestor_finanzas.dto.GrupoListDTO;
import com.finanzas.backend_gestor_finanzas.dto.MiembroGrupoDTO;
import com.finanzas.backend_gestor_finanzas.service.impl.GrupoService;
import com.finanzas.backend_gestor_finanzas.service.impl.UsuarioGrupoService;
import com.finanzas.backend_gestor_finanzas.repository.UsuarioRepository;
import com.finanzas.backend_gestor_finanzas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GrupoController {

    private final GrupoService grupoService;
    private final UsuarioGrupoService usuarioGrupoService;
    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Transactional
    public ResponseEntity<GrupoListDTO> create(@RequestBody GrupoCreateDTO dto){
        System.out.println("🚀 Iniciando creación de grupo: " + dto.getNombre());
        
        // 🔒 SEGURIDAD: Obtener el usuario autenticado actual
        Usuario usuarioActual = securityUtils.getCurrentUser();
        System.out.println("👤 Usuario actual: ID=" + usuarioActual.getId() + ", Email=" + usuarioActual.getEmail());
        
        // Crear el grupo
        Grupo grupo = Grupo.builder()
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .build();
        
        // Guardar el grupo
        Grupo grupoGuardado = grupoService.create(grupo);
        System.out.println("📝 Grupo guardado: ID=" + grupoGuardado.getId());
        
        // Añadir al creador como ADMIN del grupo
        UsuarioGrupoId adminId = new UsuarioGrupoId(usuarioActual.getId(), grupoGuardado.getId());
        UsuarioGrupo adminRelacion = UsuarioGrupo.builder()
            .id(adminId)
            .usuario(usuarioActual)
            .grupo(grupoGuardado)
            .rol("ADMIN")
            .build();
        
        UsuarioGrupo relacionGuardada = usuarioGrupoService.create(adminRelacion);
        System.out.println("🔗 Relación usuario-grupo creada: Usuario=" + relacionGuardada.getId().getUsuarioId() + 
                         ", Grupo=" + relacionGuardada.getId().getGrupoId() + ", Rol=" + relacionGuardada.getRol());
        
        System.out.println("✅ Grupo creado exitosamente: " + grupoGuardado.getId() + " por usuario: " + usuarioActual.getEmail());
        
        // Crear lista de miembros
        List<MiembroGrupoDTO> miembros = new ArrayList<>();
        miembros.add(new MiembroGrupoDTO(
            usuarioActual.getId(),
            usuarioActual.getNombre(),
            usuarioActual.getEmail(),
            "ADMIN"
        ));
        
        // Retornar DTO
        GrupoListDTO responseDto = new GrupoListDTO(
            grupoGuardado.getId(),
            grupoGuardado.getNombre(),
            grupoGuardado.getDescripcion(),
            grupoGuardado.getFechaCreacion(),
            1, // El creador es el único miembro inicial
            miembros
        );
        
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public List<GrupoListDTO> list(){
        // 🔒 SEGURIDAD: Solo devolver grupos del usuario autenticado
        Long usuarioId = securityUtils.getCurrentUserId();
        System.out.println("🔍 Consultando grupos para usuario ID: " + usuarioId);
        
        List<Grupo> grupos = grupoService.getAllByUsuario(usuarioId);
        System.out.println("📊 Grupos encontrados: " + grupos.size());
        
        grupos.forEach(g -> System.out.println("   - Grupo ID=" + g.getId() + ", Nombre=" + g.getNombre() + 
                                              ", Miembros=" + (g.getUsuarios() != null ? g.getUsuarios().size() : 0)));
        
        return grupos.stream()
            .map(grupo -> {
                // Convertir los usuarios del grupo a DTOs de miembros
                List<MiembroGrupoDTO> miembros = grupo.getUsuarios() != null 
                    ? grupo.getUsuarios().stream()
                        .map(ug -> new MiembroGrupoDTO(
                            ug.getUsuario().getId(),
                            ug.getUsuario().getNombre(),
                            ug.getUsuario().getEmail(),
                            ug.getRol()
                        ))
                        .collect(Collectors.toList())
                    : new ArrayList<>();
                
                return new GrupoListDTO(
                    grupo.getId(),
                    grupo.getNombre(),
                    grupo.getDescripcion(),
                    grupo.getFechaCreacion(),
                    grupo.getUsuarios() != null ? grupo.getUsuarios().size() : 0,
                    miembros
                );
            })
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.finanzas.backend_gestor_finanzas.dto.GrupoResponseDTO> get(@PathVariable Long id){
        return grupoService.getById(id)
            .map(grupo -> {
                var usuarios = grupo.getUsuarios().stream()
                    .map(ug -> new com.finanzas.backend_gestor_finanzas.dto.UsuarioGrupoResponseDTO(
                        ug.getId().getUsuarioId(),
                        ug.getId().getGrupoId(),
                        ug.getRol()
                    ))
                    .toList();
                var dto = new com.finanzas.backend_gestor_finanzas.dto.GrupoResponseDTO(
                    grupo.getId(),
                    grupo.getNombre(),
                    grupo.getDescripcion(),
                    grupo.getFechaCreacion(),
                    usuarios
                );
                return ResponseEntity.ok(dto);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<GrupoListDTO> update(@PathVariable Long id, @RequestBody GrupoUpdateDTO dto){
        System.out.println("🔧 Actualizando grupo ID: " + id);
        System.out.println("   Nuevo nombre: " + dto.getNombre());
        System.out.println("   Nueva descripción: " + dto.getDescripcion());
        System.out.println("   Nuevos miembros: " + (dto.getNuevosMiembros() != null ? dto.getNuevosMiembros().size() : 0));
        
        // 🔒 SEGURIDAD: Verificar que el grupo existe
        Optional<Grupo> grupoOpt = grupoService.getById(id);
        if (grupoOpt.isEmpty()) {
            System.out.println("❌ Grupo no encontrado");
            return ResponseEntity.notFound().build();
        }
        
        Grupo grupo = grupoOpt.get();
        
        // Actualizar nombre y descripción
        grupo.setNombre(dto.getNombre());
        grupo.setDescripcion(dto.getDescripcion());
        Grupo grupoActualizado = grupoService.update(id, grupo);
        System.out.println("✅ Grupo actualizado: " + grupoActualizado.getNombre());
        
        // Añadir nuevos miembros
        if (dto.getNuevosMiembros() != null && !dto.getNuevosMiembros().isEmpty()) {
            System.out.println("👥 Añadiendo " + dto.getNuevosMiembros().size() + " nuevos miembros...");
            
            for (String email : dto.getNuevosMiembros()) {
                System.out.println("   📧 Buscando usuario con email: " + email);
                
                Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
                
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    System.out.println("   ✅ Usuario encontrado: ID=" + usuario.getId() + ", Nombre=" + usuario.getNombre());
                    
                    // Verificar que no sea ya miembro
                    if (!usuarioGrupoService.getById(usuario.getId(), id).isPresent()) {
                        // Crear relación usuario-grupo
                        UsuarioGrupoId relacionId = new UsuarioGrupoId(usuario.getId(), id);
                        UsuarioGrupo nuevaRelacion = UsuarioGrupo.builder()
                            .id(relacionId)
                            .usuario(usuario)
                            .grupo(grupoActualizado)
                            .rol("MIEMBRO")
                            .build();
                        
                        usuarioGrupoService.create(nuevaRelacion);
                        System.out.println("   🔗 Miembro añadido: " + usuario.getNombre() + " como MIEMBRO");
                    } else {
                        System.out.println("   ⚠️  Usuario ya es miembro del grupo");
                    }
                } else {
                    System.out.println("   ❌ Usuario no encontrado con email: " + email);
                }
            }
        }
        
        // Recargar el grupo con todos los miembros actualizados
        grupo = grupoService.getById(id).get();
        
        // Crear lista de miembros actualizada
        List<MiembroGrupoDTO> miembros = grupo.getUsuarios() != null 
            ? grupo.getUsuarios().stream()
                .map(ug -> new MiembroGrupoDTO(
                    ug.getUsuario().getId(),
                    ug.getUsuario().getNombre(),
                    ug.getUsuario().getEmail(),
                    ug.getRol()
                ))
                .collect(Collectors.toList())
            : new ArrayList<>();
        
        System.out.println("✅ Grupo actualizado exitosamente con " + miembros.size() + " miembros");
        
        // Retornar DTO actualizado
        GrupoListDTO responseDto = new GrupoListDTO(
            grupo.getId(),
            grupo.getNombre(),
            grupo.getDescripcion(),
            grupo.getFechaCreacion(),
            miembros.size(),
            miembros
        );
        
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        grupoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
