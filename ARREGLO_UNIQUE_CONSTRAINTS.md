# 🔧 ARREGLO DE RESTRICCIONES UNIQUE EN LA BASE DE DATOS

## ⚠️ Problema Identificado

La base de datos tenía restricciones `UNIQUE` **incorrectas** que impedían que diferentes usuarios tuvieran elementos con el mismo nombre.

### 🔴 Problema Anterior:

```
✅ Usuario 10 crea bolsillo "Comida"
❌ Usuario 3 intenta crear bolsillo "Comida" → ERROR: Ya existe

✅ Usuario 10 crea categoría "Transporte"
❌ Usuario 3 intenta crear categoría "Transporte" → ERROR: Ya existe
```

**Causa**: La restricción `UNIQUE` estaba solo en la columna `nombre`, aplicándose **globalmente** a toda la tabla.

---

## ✅ Solución Implementada

Cambié las restricciones `UNIQUE` para que sean **compuestas**, incluyendo el usuario o grupo:

### 📋 Nuevas Restricciones:

#### 1. **Tabla `bolsillo`**

```sql
-- Restricción para bolsillos personales
UNIQUE (nombre, id_usuario)

-- Restricción para bolsillos de grupo
UNIQUE (nombre, id_grupo)
```

**Resultado:**
- ✅ Usuario 10 puede tener un bolsillo "Comida"
- ✅ Usuario 3 puede tener un bolsillo "Comida"
- ❌ Usuario 10 NO puede tener dos bolsillos "Comida"

#### 2. **Tabla `categoria`**

```sql
-- Restricción para categorías personales
UNIQUE (nombre, tipo, id_usuario)

-- Restricción para categorías de grupo
UNIQUE (nombre, tipo, id_grupo)
```

**Resultado:**
- ✅ Usuario 10 puede tener categoría "Transporte" (tipo: egreso)
- ✅ Usuario 3 puede tener categoría "Transporte" (tipo: egreso)
- ✅ Usuario 10 puede tener categoría "Transporte" (tipo: ingreso)
- ❌ Usuario 10 NO puede tener dos categorías "Transporte" del mismo tipo

---

## 🚀 Cómo Aplicar los Cambios

### Opción 1: Dejar que Hibernate actualice automáticamente

Si tienes `spring.jpa.hibernate.ddl-auto=update` en `application.properties`:

```bash
# Simplemente ejecuta el backend
./mvnw spring-boot:run
```

Hibernate detectará los cambios en los modelos y actualizará la base de datos automáticamente.

### Opción 2: Ejecutar el script SQL manualmente

```bash
# Conectarse a MySQL
mysql -u root -p

# Ejecutar el script
source migration_unique_constraints.sql
```

O desde MySQL Workbench:
1. Abre el archivo `migration_unique_constraints.sql`
2. Ejecuta todo el script

---

## 📝 Cambios en el Código

### Modelo `Bolsillo.java`

```java
@Entity
@Table(name = "bolsillo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "id_usuario"}, 
                      name = "uk_bolsillo_nombre_usuario"),
    @UniqueConstraint(columnNames = {"nombre", "id_grupo"}, 
                      name = "uk_bolsillo_nombre_grupo")
})
public class Bolsillo {
    // ...
}
```

### Modelo `Categoria.java`

```java
@Entity
@Table(name = "categoria", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "tipo", "id_usuario"}, 
                      name = "uk_categoria_nombre_tipo_usuario"),
    @UniqueConstraint(columnNames = {"nombre", "tipo", "id_grupo"}, 
                      name = "uk_categoria_nombre_tipo_grupo")
})
public class Categoria {
    // ...
}
```

---

## 🧪 Verificación

Después de aplicar los cambios, verifica que funciona correctamente:

### Test 1: Crear bolsillos con el mismo nombre

```bash
# Login como Usuario 1
POST /api/auth/login
{
  "email": "usuario1@test.com",
  "contrasena": "pass123"
}

# Crear bolsillo "Comida"
POST /api/bolsillos
Authorization: Bearer <token_usuario1>
{
  "nombre": "Comida",
  "saldo": 1000
}

# Login como Usuario 2
POST /api/auth/login
{
  "email": "usuario2@test.com",
  "contrasena": "pass123"
}

# Crear bolsillo "Comida" - Debería funcionar ✅
POST /api/bolsillos
Authorization: Bearer <token_usuario2>
{
  "nombre": "Comida",
  "saldo": 2000
}
```

### Test 2: Duplicado del mismo usuario

```bash
# Usuario 1 intenta crear otro bolsillo "Comida"
POST /api/bolsillos
Authorization: Bearer <token_usuario1>
{
  "nombre": "Comida",
  "saldo": 3000
}

# Debería fallar con error: ❌ Duplicate entry
```

---

## 📊 Comparación: Antes vs Ahora

| Escenario | Antes | Ahora |
|-----------|-------|-------|
| Usuario 1 crea bolsillo "Comida" | ✅ OK | ✅ OK |
| Usuario 2 crea bolsillo "Comida" | ❌ ERROR | ✅ OK |
| Usuario 1 crea otro bolsillo "Comida" | ❌ ERROR | ❌ ERROR |
| Usuario 1 crea categoría "Transporte" (egreso) | ✅ OK | ✅ OK |
| Usuario 2 crea categoría "Transporte" (egreso) | ❌ ERROR | ✅ OK |
| Usuario 1 crea categoría "Transporte" (ingreso) | ❌ ERROR | ✅ OK |

---

## ⚠️ Consideraciones Importantes

### 1. **Datos Existentes**

Si ya tienes datos en la base de datos que violan las nuevas restricciones:

```sql
-- Verificar duplicados en bolsillos
SELECT nombre, id_usuario, COUNT(*) 
FROM bolsillo 
WHERE id_usuario IS NOT NULL
GROUP BY nombre, id_usuario 
HAVING COUNT(*) > 1;

-- Verificar duplicados en categorías
SELECT nombre, tipo, id_usuario, COUNT(*) 
FROM categoria 
WHERE id_usuario IS NOT NULL
GROUP BY nombre, tipo, id_usuario 
HAVING COUNT(*) > 1;
```

### 2. **Manejo de Errores**

Actualiza tus controladores para manejar errores de duplicados:

```java
@PostMapping
public ResponseEntity<?> create(@RequestBody BolsilloCreateDTO dto) {
    try {
        Bolsillo bolsillo = Bolsillo.builder()
            .nombre(dto.getNombre())
            .saldo(dto.getSaldo())
            .usuario(securityUtils.getCurrentUser())
            .build();
        
        Bolsillo creado = bolsilloService.create(bolsillo);
        return ResponseEntity.ok(creado);
        
    } catch (DataIntegrityViolationException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body("Ya tienes un bolsillo con ese nombre");
    }
}
```

---

## 🎯 Beneficios

✅ **Privacidad**: Cada usuario maneja sus datos independientemente  
✅ **Flexibilidad**: Los usuarios pueden usar los nombres que quieran  
✅ **Escalabilidad**: No hay conflictos entre usuarios  
✅ **UX Mejorada**: Los usuarios no ven errores confusos  

---

## 📚 Referencias

- [JPA UniqueConstraint Documentation](https://jakarta.ee/specifications/persistence/3.0/apidocs/jakarta.persistence/jakarta/persistence/uniqueconstraint)
- [MySQL UNIQUE Constraints](https://dev.mysql.com/doc/refman/8.0/en/constraint-unique.html)
- [Hibernate DDL Auto](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data.spring.jpa.hibernate.ddl-auto)

---

**Fecha:** 18 de octubre de 2025  
**Estado:** ✅ IMPLEMENTADO  
**Prioridad:** 🔴 ALTA
