-- Migración 003 (v0.6.0): descuentos en cotizaciones (por ítem y global)
-- Ejecutar UNA vez en MySQL Workbench conectado como root, DESPUÉS de migracion-002-auditoria.sql.
-- Las cotizaciones existentes quedan con descuento 0, así que sus totales no cambian.
USE cruzma_ventas;

ALTER TABLE detalle_cotizacion
  ADD COLUMN descuento DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (descuento BETWEEN 0 AND 100) AFTER precio;

ALTER TABLE cotizacion
  ADD COLUMN descuento DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (descuento BETWEEN 0 AND 100) AFTER estado;

-- Historial de auditoría (Hibernate Envers): la tabla cotizacion_aud debe tener la misma columna
ALTER TABLE cotizacion_aud ADD COLUMN descuento DECIMAL(5,2) AFTER estado;

-- Comprobación
SHOW COLUMNS FROM cotizacion LIKE 'descuento';
SHOW COLUMNS FROM detalle_cotizacion LIKE 'descuento';
SHOW COLUMNS FROM cotizacion_aud LIKE 'descuento';
