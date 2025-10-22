# 🔧 Arreglos Necesarios en el Backend

## ⚠️ Problemas Principales

### 1. El backend devuelve datos de TODOS los usuarios
El backend está devolviendo **TODOS los datos de TODOS los usuarios** en vez de filtrar por el usuario autenticado. Esto es un **problema de seguridad grave** y de privacidad.

### 2. Restricción UNIQUE incorrecta en la base de datos
**PROBLEMA CRÍTICO**: La base de datos tiene restricciones UNIQUE en los nombres (ej: nombre del bolsillo, nombre de categoría) a nivel GLOBAL, cuando debería ser **UNIQUE por usuario**.

**Ejemplo del problema:**
- Usuario 10 crea un bolsillo "Comida" ✅
- Usuario 3 intenta crear un bolsillo "Comida" ❌ ERROR: Ya existe

**Debería ser:**
- Usuario 10 crea un bolsillo "Comida" ✅
- Usuario 3 crea un bolsillo "Comida" ✅ (es su propio bolsillo)
- Ambos bolsillos existen independientemente

## 🎯 Solución Aplicada en el Frontend (TEMPORAL)

He implementado un filtro en el frontend (`app.js`) que filtra los datos por `usuarioId`, pero esto es solo una **solución temporal**. El backend DEBE ser arreglado.

## 📋 Endpoints que DEBEN ser arreglados en el Backend

### 1. GET `/api/categorias` ❌
**Problema:** Devuelve todas las categorías de todos los usuarios.

**Solución requerida:**
```javascript
// El backend debe:
// 1. Leer el token JWT del header Authorization
// 2. Extraer el usuarioId del token
// 3. Filtrar la consulta SQL:

SELECT * FROM categorias WHERE usuarioId = ? // ID del usuario del token
```

### 2. GET `/api/bolsillos` ❌
**Problema:** Devuelve todos los bolsillos de todos los usuarios.

**Solución requerida:**
```javascript
SELECT * FROM bolsillos WHERE usuarioId = ?
```

### 3. GET `/api/ingresos` ❌
**Problema:** Devuelve todos los ingresos de todos los usuarios.

**Solución requerida:**
```javascript
SELECT * FROM ingresos WHERE usuarioId = ?
```

### 4. GET `/api/egresos` ❌
**Problema:** Devuelve todos los egresos de todos los usuarios.

**Solución requerida:**
```javascript
SELECT * FROM egresos WHERE usuarioId = ?
```

### 5. GET `/api/grupos` ❌
**Problema:** Devuelve todos los grupos de todos los usuarios.

**Solución requerida:**
```javascript
SELECT * FROM grupos WHERE usuarioId = ?
```

## 🔐 Cómo Implementar la Solución en el Backend

### Paso 1: Middleware de Autenticación
Crear un middleware que extraiga el usuario del token JWT:

```javascript
// middleware/auth.js
const jwt = require('jsonwebtoken');

const authMiddleware = (req, res, next) => {
    try {
        // Obtener el token del header
        const token = req.headers.authorization?.replace('Bearer ', '');
        
        if (!token) {
            return res.status(401).json({ error: 'Token no proporcionado' });
        }
        
        // Verificar y decodificar el token
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        // Agregar el usuario al request
        req.usuario = decoded; // Debe contener { id: usuarioId, ... }
        
        next();
    } catch (error) {
        return res.status(401).json({ error: 'Token inválido' });
    }
};

module.exports = authMiddleware;
```

### Paso 2: Aplicar el Middleware y Filtrar por Usuario

```javascript
// routes/categorias.js
const authMiddleware = require('../middleware/auth');

// GET /api/categorias - Listar categorías del usuario
router.get('/categorias', authMiddleware, async (req, res) => {
    try {
        const usuarioId = req.usuario.id; // Extraído del token JWT
        
        const categorias = await db.query(
            'SELECT * FROM categorias WHERE usuarioId = ?',
            [usuarioId]
        );
        
        res.json(categorias);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /api/categorias - Crear categoría
router.post('/categorias', authMiddleware, async (req, res) => {
    try {
        const usuarioId = req.usuario.id; // Extraído del token JWT
        const { nombre, tipo } = req.body;
        
        // NO confiar en el usuarioId del body, usar el del token
        const result = await db.query(
            'INSERT INTO categorias (nombre, tipo, usuarioId) VALUES (?, ?, ?)',
            [nombre, tipo, usuarioId]
        );
        
        res.status(201).json({ id: result.insertId, nombre, tipo, usuarioId });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});
```

### Paso 3: Aplicar lo Mismo a TODOS los Endpoints

Repetir la misma lógica para:
- ✅ `/api/bolsillos`
- ✅ `/api/ingresos`
- ✅ `/api/egresos`
- ✅ `/api/grupos`

## �️ Arreglo CRÍTICO en la Base de Datos

### Problema: Restricciones UNIQUE incorrectas

Actualmente, las tablas tienen restricciones UNIQUE solo en el nombre:

```sql
-- ❌ INCORRECTO - Solo puede haber un bolsillo "Comida" en toda la BD
CREATE TABLE bolsillos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE,  -- ❌ MALO
    saldo DECIMAL(10,2),
    usuarioId INT,
    FOREIGN KEY (usuarioId) REFERENCES usuarios(id)
);
```

### Solución: UNIQUE compuesto (nombre + usuarioId)

```sql
-- ✅ CORRECTO - Cada usuario puede tener su propio bolsillo "Comida"
CREATE TABLE bolsillos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    saldo DECIMAL(10,2),
    usuarioId INT NOT NULL,
    FOREIGN KEY (usuarioId) REFERENCES usuarios(id),
    UNIQUE KEY unique_bolsillo_por_usuario (usuarioId, nombre)  -- ✅ CORRECTO
);
```

### Aplicar a TODAS las tablas:

#### 1. Tabla `categorias`
```sql
ALTER TABLE categorias 
DROP INDEX IF EXISTS nombre,
ADD UNIQUE KEY unique_categoria_por_usuario (usuarioId, nombre);
```

#### 2. Tabla `bolsillos`
```sql
ALTER TABLE bolsillos 
DROP INDEX IF EXISTS nombre,
ADD UNIQUE KEY unique_bolsillo_por_usuario (usuarioId, nombre);
```

#### 3. Tabla `grupos`
```sql
ALTER TABLE grupos 
DROP INDEX IF EXISTS nombre,
ADD UNIQUE KEY unique_grupo_por_usuario (usuarioId, nombre);
```

### Verificar restricciones actuales

```sql
-- Ver las restricciones actuales de cada tabla
SHOW CREATE TABLE categorias;
SHOW CREATE TABLE bolsillos;
SHOW CREATE TABLE grupos;
```

## �🚨 Importante

1. **NUNCA confiar en el `usuarioId` que viene en el body del request**
2. **SIEMPRE extraer el `usuarioId` del token JWT**
3. **SIEMPRE filtrar por `usuarioId` en las consultas SELECT**
4. **SIEMPRE usar el `usuarioId` del token en las consultas INSERT**
5. **Cambiar las restricciones UNIQUE en la base de datos** para permitir nombres duplicados entre diferentes usuarios

## ✅ Verificación

Una vez arreglado el backend, puedes verificar que funciona correctamente:

1. Abrir la consola del navegador (F12)
2. Buscar las advertencias que dicen: "⚠️ El backend está devolviendo datos de otros usuarios!"
3. Si el backend está arreglado, **NO deberías ver esas advertencias**
4. Los logs mostrarán: "0 bolsillos de otros usuarios", "0 categorías de otros usuarios", etc.

## 📝 Nota para el Desarrollador

El filtro en el frontend es una **medida de seguridad temporal**. Un usuario malintencionado podría modificar el código JavaScript y ver los datos de otros usuarios. Por eso es **crítico** arreglar el backend lo antes posible.

---

**Última actualización:** 18 de octubre de 2025
**Responsable:** Equipo de Backend
**Prioridad:** 🔴 ALTA (Problema de Seguridad)
