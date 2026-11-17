-- Usuario de aplicación para MySQL en la nube (Railway o Azure Database for MySQL).
-- Ejecutar DESPUÉS de db/schema.sql, conectado con el usuario administrador (root en Railway).
-- En la nube la aplicación no se conecta desde "localhost", por eso el usuario es 'cruzma_app'@'%'.
-- 1) Reemplaza ESCRIBE_AQUI_UNA_CLAVE por una contraseña nueva (mínimo 12 caracteres, con mayúsculas, números y símbolos).
-- 2) Ejecuta el script. 3) Pon esa misma contraseña en la variable DB_PASSWORD de la aplicación web.
-- 4) NO guardes este archivo con la contraseña escrita ni lo subas a GitHub: vuelve a dejar el texto de ejemplo.

CREATE USER IF NOT EXISTS 'cruzma_app'@'%' IDENTIFIED BY 'ESCRIBE_AQUI_UNA_CLAVE';
GRANT SELECT, INSERT, UPDATE ON cruzma_ventas.* TO 'cruzma_app'@'%';

-- El usuario 'cruzma_app'@'localhost' que crea schema.sql no se usa en la nube; se elimina para no dejar una clave conocida.
DROP USER IF EXISTS 'cruzma_app'@'localhost';
