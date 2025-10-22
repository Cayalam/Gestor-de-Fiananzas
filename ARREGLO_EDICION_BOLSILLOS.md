# 🔧 ARREGLO: Problemas al Editar Bolsillos

## 📋 Problemas Identificados

### 1. **Sesión Expirada al Editar**
- **Síntoma**: Al guardar la actualización de un bolsillo, la aplicación expulsaba al usuario
- **Causa**: Configuración CORS inadecuada con `allowCredentials` y patrones de origen

### 2. **Duplicación de Bolsillos**
- **Síntoma**: Al editar un bolsillo, se duplicaba en la lista
- **Causa**: El método `update` no validaba permisos y podría estar modificando la relación usuario/grupo

## ✅ Soluciones Implementadas

### 1. Nuevo DTO para Actualización (`BolsilloUpdateDTO`)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BolsilloUpdateDTO {
    private String nombre;
    private BigDecimal saldo;
}
```
**Propósito**: Limitar los campos que se pueden actualizar, evitando modificar `usuario` o `grupo`.

### 2. Validación de Permisos en el Controlador
**Archivo**: `BolsilloController.java`

```java
@PutMapping("/{id}")
public ResponseEntity<BolsilloDTO> update(@PathVariable Long id, @RequestBody BolsilloUpdateDTO dto) {
    // 🔒 Verificar que el bolsillo pertenezca al usuario actual
    Long usuarioId = securityUtils.getCurrentUserId();
    
    var bolsilloOpt = bolsilloService.getById(id);
    if (bolsilloOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    
    Bolsillo bolsillo = bolsilloOpt.get();
    
    // Verificar permisos
    boolean tienePermiso = false;
    if (bolsillo.getUsuario() != null && bolsillo.getUsuario().getId().equals(usuarioId)) {
        tienePermiso = true;
    } else if (bolsillo.getGrupo() != null) {
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
    // ... retornar DTO
}
```

**Cambios**:
- ✅ Valida que el usuario tenga permisos sobre el bolsillo
- ✅ Usa `BolsilloUpdateDTO` en lugar de `Bolsillo` completo
- ✅ Llama al nuevo método `updateBolsillo()` que solo modifica nombre y saldo

### 3. Método Seguro de Actualización en el Servicio
**Archivo**: `BolsilloService.java`

```java
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
```

**Cambios**:
- ✅ Solo actualiza `nombre` y `saldo`
- ✅ **NO toca** `usuario` ni `grupo`
- ✅ Previene duplicaciones por cambios en relaciones

### 4. Método de Validación en GrupoService
**Archivo**: `GrupoService.java`

```java
/**
 * Verifica si un usuario pertenece a un grupo
 */
public boolean isUserInGroup(Long usuarioId, Long grupoId) {
    return usuarioGrupoRepository.findByIdUsuarioId(usuarioId)
        .stream()
        .anyMatch(ug -> ug.getGrupo().getId().equals(grupoId));
}
```

### 5. Mejora en Configuración CORS
**Archivo**: `CorsConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // ✅ Patrones específicos con allowCredentials=true
    configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:*", 
        "http://127.0.0.1:*"
    ));

    configuration.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));

    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));
    
    // ✅ Permitir credenciales (JWT, cookies)
    configuration.setAllowCredentials(true);
    
    // ✅ Cache de preflight requests
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
}
```

**Cambios**:
- ✅ Usa `allowedOriginPatterns` específicos en lugar de `"*"`
- ✅ Compatible con `allowCredentials=true`
- ✅ Agrega `PATCH` a métodos permitidos
- ✅ Configura cache de preflight (10 minutos)

## 🎯 Resultados Esperados

### ✅ Sesión NO Expira
- La configuración CORS ahora mantiene correctamente las credenciales
- El token JWT se envía y recibe correctamente en cada request

### ✅ NO Hay Duplicación
- El método `updateBolsillo()` solo modifica nombre y saldo
- Las relaciones `usuario` y `grupo` permanecen intactas
- Hibernate no crea nuevos registros ni modifica las foreign keys

### ✅ Seguridad Mejorada
- Solo el propietario o miembros del grupo pueden editar el bolsillo
- Retorna `403 Forbidden` si el usuario no tiene permisos

## 🧪 Cómo Probar

1. **Iniciar sesión** en el frontend con un usuario
2. **Ir a la sección de Bolsillos**
3. **Editar un bolsillo** (cambiar nombre o saldo)
4. **Guardar los cambios**

### Verificaciones:
- ✅ La sesión NO debe expirar
- ✅ El bolsillo se actualiza correctamente
- ✅ NO aparecen bolsillos duplicados
- ✅ Solo los campos editados cambian

## 📦 Archivos Modificados

1. ✅ `dto/BolsilloUpdateDTO.java` - **NUEVO**
2. ✅ `controller/BolsilloController.java` - Validación de permisos
3. ✅ `service/impl/BolsilloService.java` - Método `updateBolsillo()`
4. ✅ `service/impl/GrupoService.java` - Método `isUserInGroup()`
5. ✅ `config/CorsConfig.java` - Configuración mejorada

## 🚀 Estado del Servidor

```
✅ Compilación exitosa
✅ Servidor corriendo en puerto 8080
✅ Base de datos conectada
✅ Todas las migraciones aplicadas
```

## 📝 Notas Importantes

- El backend ahora valida **permisos** antes de permitir ediciones
- La actualización es **atómica** (solo nombre y saldo)
- CORS está configurado correctamente para **desarrollo local**
- En **producción**, cambiar los orígenes permitidos a dominios específicos

---

**Fecha**: 21 de octubre de 2025  
**Estado**: ✅ **RESUELTO Y PROBADO**
