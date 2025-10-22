# 🔒 ARREGLO DE SEGURIDAD - Backend Gestor Finanzas

## 📋 Resumen de Cambios

Se ha implementado una **solución de seguridad crítica** que corrige el problema donde el backend devolvía **TODOS los datos de TODOS los usuarios** sin filtrar por el usuario autenticado.

**Fecha:** 18 de octubre de 2025  
**Prioridad:** 🔴 ALTA (Problema de Seguridad Crítico)

---

## 🛠️ Cambios Implementados

### 1. ✅ JWT Mejorado para Incluir `usuarioId`

**Archivo:** `security/JwtUtil.java`

- ✨ **Nuevo:** El token JWT ahora incluye el `usuarioId` como claim adicional
- ✨ **Nuevo:** Método `getUserIdFromToken(String token)` para extraer el ID del usuario
- ✨ **Modificado:** `generateToken()` ahora requiere `email` y `usuarioId`

```java
// Antes
public String generateToken(String email)

// Ahora
public String generateToken(String email, Long usuarioId)
public Long getUserIdFromToken(String token)
```

---

### 2. ✅ AuthController Actualizado

**Archivo:** `controller/AuthController.java`

- ✨ **Modificado:** El endpoint `/api/auth/login` ahora genera tokens con `usuarioId`
- ✨ **Modificado:** La respuesta del login incluye el `usuarioId` del usuario autenticado

```java
// Respuesta del login ahora incluye:
{
  "token": "eyJhbGci...",
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "usuarioId": 1
}
```

---

### 3. ✅ Nueva Clase `SecurityUtils`

**Archivo:** `security/SecurityUtils.java` (NUEVO)

Utilidad centralizada para obtener el usuario autenticado desde el contexto de seguridad.

**Métodos disponibles:**
- `getCurrentUser()` - Retorna el objeto `Usuario` completo
- `getCurrentUserId()` - Retorna el ID del usuario autenticado
- `getCurrentUserEmail()` - Retorna el email del usuario autenticado

---

### 4. ✅ Repositorios Actualizados

Se agregaron queries JPQL para filtrar datos por usuario y sus grupos:

**Archivos modificados:**
- `repository/CategoriaRepository.java`
- `repository/BolsilloRepository.java`
- `repository/IngresoRepository.java`
- `repository/EgresoRepository.java`

**Nuevo método en cada repositorio:**
```java
@Query("SELECT e FROM Entity e WHERE e.usuario.id = :usuarioId OR e.grupo.id IN :grupoIds")
List<Entity> findByUsuarioIdOrGrupoIds(@Param("usuarioId") Long usuarioId, 
                                        @Param("grupoIds") List<Long> grupoIds);
```

---

### 5. ✅ Servicios Actualizados

Todos los servicios ahora tienen un método `getAllByUsuario(Long usuarioId)` que:
1. Obtiene los grupos del usuario
2. Filtra los datos por usuario Y por sus grupos

**Archivos modificados:**
- `service/impl/CategoriaService.java`
- `service/impl/BolsilloService.java`
- `service/impl/IngresoService.java`
- `service/impl/EgresoService.java`
- `service/impl/GrupoService.java`

---

### 6. ✅ Controladores Actualizados con Seguridad

Todos los controladores fueron modificados para:

#### 🔒 En endpoints GET:
- Extraer el `usuarioId` del usuario autenticado usando `SecurityUtils`
- Llamar a `service.getAllByUsuario(usuarioId)` en lugar de `service.getAll()`
- **NUNCA** confiar en parámetros del request

#### 🔒 En endpoints POST:
- Establecer el `usuario` desde `SecurityUtils.getCurrentUser()`
- **NUNCA** confiar en el `usuarioId` que venga en el body del request

**Archivos modificados:**
- `controller/CategoriaController.java`
- `controller/BolsilloController.java`
- `controller/IngresoController.java`
- `controller/EgresoController.java`
- `controller/GrupoController.java`

**Ejemplo de implementación:**

```java
@GetMapping
public List<CategoriaDTO> list(){
    // 🔒 SEGURIDAD: Solo devolver categorías del usuario autenticado
    Long usuarioId = securityUtils.getCurrentUserId();
    return categoriaService.getAllByUsuario(usuarioId).stream()
        .map(...)
        .toList();
}

@PostMapping
public ResponseEntity<Categoria> create(@RequestBody Categoria categoria){
    // 🔒 SEGURIDAD: Usar el usuario autenticado del token, NO del body
    categoria.setUsuario(securityUtils.getCurrentUser());
    return ResponseEntity.ok(categoriaService.create(categoria));
}
```

---

## 📊 Endpoints Arreglados

| Endpoint | Método | Estado |
|----------|--------|--------|
| `/api/categorias` | GET | ✅ ARREGLADO |
| `/api/categorias` | POST | ✅ ARREGLADO |
| `/api/bolsillos` | GET | ✅ ARREGLADO |
| `/api/bolsillos` | POST | ✅ ARREGLADO |
| `/api/ingresos` | GET | ✅ ARREGLADO |
| `/api/ingresos` | POST | ✅ ARREGLADO |
| `/api/egresos` | GET | ✅ ARREGLADO |
| `/api/egresos` | POST | ✅ ARREGLADO |
| `/api/grupos` | GET | ✅ ARREGLADO |

---

## 🔐 Principios de Seguridad Aplicados

### ✅ DO (Hacer):
1. ✅ **SIEMPRE** extraer el `usuarioId` del token JWT
2. ✅ **SIEMPRE** filtrar consultas por el usuario autenticado
3. ✅ **SIEMPRE** usar `SecurityUtils.getCurrentUser()` para obtener el usuario
4. ✅ **SIEMPRE** validar permisos en el backend

### ❌ DON'T (No Hacer):
1. ❌ **NUNCA** confiar en el `usuarioId` del body del request
2. ❌ **NUNCA** usar `service.getAll()` directamente en endpoints públicos
3. ❌ **NUNCA** depender del filtrado en el frontend
4. ❌ **NUNCA** exponer datos de otros usuarios

---

## 🧪 Cómo Verificar que Funciona

### 1. Prueba Manual

1. **Iniciar el backend:**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Login con Usuario 1:**
   ```bash
   POST http://localhost:8080/api/auth/login
   {
     "email": "user1@example.com",
     "contrasena": "password123"
   }
   ```
   
   Recibirás un token JWT.

3. **Obtener categorías del Usuario 1:**
   ```bash
   GET http://localhost:8080/api/categorias
   Authorization: Bearer <token_usuario_1>
   ```
   
   ✅ Deberías ver **SOLO** las categorías del Usuario 1

4. **Login con Usuario 2 y repetir:**
   ```bash
   GET http://localhost:8080/api/categorias
   Authorization: Bearer <token_usuario_2>
   ```
   
   ✅ Deberías ver **SOLO** las categorías del Usuario 2

### 2. Verificación en el Frontend

Si el backend está arreglado correctamente:

1. Abre la consola del navegador (F12)
2. Busca los logs de advertencia
3. ✅ **NO deberías ver:** "⚠️ El backend está devolviendo datos de otros usuarios!"
4. ✅ **Deberías ver:** "0 bolsillos de otros usuarios", "0 categorías de otros usuarios", etc.

---

## 🚀 Pasos Siguientes

1. ✅ **Eliminar el filtrado temporal del frontend** en `app.js`
2. ✅ **Ejecutar tests de integración** para verificar que todo funciona
3. ✅ **Desplegar a producción** lo antes posible

---

## 📝 Notas Importantes

- ⚠️ **El filtrado en el frontend era temporal:** Un usuario malintencionado podía modificar el JavaScript y ver datos de otros usuarios
- ✅ **Ahora la seguridad está en el backend:** Donde debe estar
- ✅ **Los datos personales están protegidos:** Cada usuario solo ve sus propios datos
- ✅ **Los datos de grupos están incluidos:** Si un usuario pertenece a un grupo, verá los datos compartidos del grupo

---

## 🔄 Compatibilidad con el Frontend

Los cambios en el backend son **compatibles** con el frontend actual:

- ✅ La estructura de los DTOs no cambió
- ✅ Los endpoints siguen siendo los mismos
- ✅ Solo el comportamiento de filtrado cambió (ahora correcto)
- ⚠️ **IMPORTANTE:** El frontend debe enviar el token JWT en todas las peticiones

**Formato del header:**
```
Authorization: Bearer <token_jwt>
```

---

## 👨‍💻 Desarrollador

**Responsable:** Equipo de Backend  
**Revisado por:** GitHub Copilot  
**Estado:** ✅ COMPLETADO

---

## 📚 Referencias

- [JWT Best Practices](https://jwt.io/introduction)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [OWASP Top 10 - Broken Access Control](https://owasp.org/Top10/A01_2021-Broken_Access_Control/)
