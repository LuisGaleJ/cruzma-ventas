-- Migración 004 (v0.7.0): unidades de medida con códigos de la Tabla 6 de SUNAT
-- Convierte las unidades escritas a mano (UND, M, M2, SERV...) al código oficial (NIU, MTR, MTK, ZZ...).
-- Ejecutar UNA vez conectado como root: en Workbench (PCs) o en Railway → MySQL → Database → Data.
-- No cambia precios, cotizaciones ni ventas: solo la columna unidad de los productos.
USE cruzma_ventas;

-- SET SQL_SAFE_UPDATES = 0;   -- descomentar si Workbench bloquea el UPDATE por "safe update mode"

UPDATE producto SET unidad = CASE
    WHEN tipo = 'SERVICIO'                              THEN 'ZZ'
    WHEN UPPER(TRIM(unidad)) IN ('UND', 'UNID', 'U')    THEN 'NIU'
    WHEN UPPER(TRIM(unidad)) IN ('M', 'ML')             THEN 'MTR'
    WHEN UPPER(TRIM(unidad)) IN ('M2', 'M²')            THEN 'MTK'
    WHEN UPPER(TRIM(unidad)) IN ('M3', 'M³')            THEN 'MTQ'
    WHEN UPPER(TRIM(unidad)) IN ('KG')                  THEN 'KGM'
    WHEN UPPER(TRIM(unidad)) IN ('JGO', 'JUEGO')        THEN 'SET'
    WHEN UPPER(TRIM(unidad)) IN ('PZA')                 THEN 'C62'
    WHEN UPPER(TRIM(unidad)) IN ('GL', 'GAL')           THEN 'GLL'
    WHEN UPPER(TRIM(unidad)) IN ('LT')                  THEN 'LTR'
    WHEN UPPER(TRIM(unidad)) IN ('NIU','MTR','MTK','MTQ','KGM','TNE','C62','PG','TU','SET','PR','KT','BX','PK','GLL','LTR')
                                                        THEN UPPER(TRIM(unidad))
    ELSE 'NIU'                                          -- cualquier otra unidad de producto queda como unidad (bienes)
END
WHERE id_producto > 0;

-- Comprobación: todas las unidades deben ser códigos de la lista
SELECT unidad, tipo, COUNT(*) AS productos FROM producto GROUP BY unidad, tipo ORDER BY tipo, unidad;
