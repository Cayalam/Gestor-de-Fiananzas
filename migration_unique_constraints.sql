-- =====================================================
-- SCRIPT DE MIGRACIÓN - ARREGLO DE RESTRICCIONES UNIQUE
-- =====================================================
-- Fecha: 18 de octubre de 2025
-- Propósito: Corregir restricciones UNIQUE para que sean
--            por usuario en lugar de globales
-- =====================================================

USE finanzasdb_2;

-- =====================================================
-- 1. TABLA BOLSILLO
-- =====================================================

-- Eliminar restricción UNIQUE global en nombre (si existe)
SET @sql = (
    SELECT CONCAT('ALTER TABLE bolsillo DROP INDEX ', constraint_name, ';')
    FROM information_schema.table_constraints
    WHERE table_schema = 'finanzasdb_2'
      AND table_name = 'bolsillo'
      AND constraint_type = 'UNIQUE'
      AND constraint_name != 'uk_bolsillo_nombre_usuario'
      AND constraint_name != 'uk_bolsillo_nombre_grupo'
    LIMIT 1
);

SET @sql = IFNULL(@sql, 'SELECT "No hay restricciones UNIQUE antiguas en bolsillo"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar restricciones UNIQUE compuestas correctas
ALTER TABLE bolsillo
    ADD CONSTRAINT uk_bolsillo_nombre_usuario 
    UNIQUE (nombre, id_usuario);

ALTER TABLE bolsillo
    ADD CONSTRAINT uk_bolsillo_nombre_grupo 
    UNIQUE (nombre, id_grupo);

SELECT '✅ Tabla BOLSILLO actualizada correctamente' AS Status;

-- =====================================================
-- 2. TABLA CATEGORIA
-- =====================================================

-- Eliminar restricción UNIQUE global en nombre (si existe)
SET @sql = (
    SELECT CONCAT('ALTER TABLE categoria DROP INDEX ', constraint_name, ';')
    FROM information_schema.table_constraints
    WHERE table_schema = 'finanzasdb_2'
      AND table_name = 'categoria'
      AND constraint_type = 'UNIQUE'
      AND constraint_name != 'uk_categoria_nombre_tipo_usuario'
      AND constraint_name != 'uk_categoria_nombre_tipo_grupo'
    LIMIT 1
);

SET @sql = IFNULL(@sql, 'SELECT "No hay restricciones UNIQUE antiguas en categoria"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Agregar restricciones UNIQUE compuestas correctas
ALTER TABLE categoria
    ADD CONSTRAINT uk_categoria_nombre_tipo_usuario 
    UNIQUE (nombre, tipo, id_usuario);

ALTER TABLE categoria
    ADD CONSTRAINT uk_categoria_nombre_tipo_grupo 
    UNIQUE (nombre, tipo, id_grupo);

SELECT '✅ Tabla CATEGORIA actualizada correctamente' AS Status;

-- =====================================================
-- 3. VERIFICACIÓN
-- =====================================================

-- Verificar restricciones de BOLSILLO
SELECT 
    'BOLSILLO' AS Tabla,
    constraint_name AS Restriccion,
    GROUP_CONCAT(column_name ORDER BY ordinal_position) AS Columnas
FROM information_schema.key_column_usage
WHERE table_schema = 'finanzasdb_2'
  AND table_name = 'bolsillo'
  AND constraint_name LIKE 'uk_%'
GROUP BY constraint_name;

-- Verificar restricciones de CATEGORIA
SELECT 
    'CATEGORIA' AS Tabla,
    constraint_name AS Restriccion,
    GROUP_CONCAT(column_name ORDER BY ordinal_position) AS Columnas
FROM information_schema.key_column_usage
WHERE table_schema = 'finanzasdb_2'
  AND table_name = 'categoria'
  AND constraint_name LIKE 'uk_%'
GROUP BY constraint_name;

SELECT '
=====================================================
✅ MIGRACIÓN COMPLETADA EXITOSAMENTE
=====================================================

CAMBIOS REALIZADOS:
-------------------
1. BOLSILLO:
   - Eliminada restricción UNIQUE global en "nombre"
   - Agregada restricción UNIQUE(nombre, id_usuario)
   - Agregada restricción UNIQUE(nombre, id_grupo)
   
2. CATEGORIA:
   - Eliminada restricción UNIQUE global en "nombre"
   - Agregada restricción UNIQUE(nombre, tipo, id_usuario)
   - Agregada restricción UNIQUE(nombre, tipo, id_grupo)

RESULTADO:
----------
✅ Ahora cada usuario puede crear sus propios bolsillos
   y categorías con nombres independientes
   
EJEMPLO:
--------
Usuario 10: Bolsillo "Comida" ✅
Usuario 3:  Bolsillo "Comida" ✅
Usuario 10: Categoría "Transporte" (tipo: eg) ✅
Usuario 3:  Categoría "Transporte" (tipo: eg) ✅

=====================================================
' AS RESUMEN;
