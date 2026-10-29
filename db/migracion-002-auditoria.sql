-- Migración 002 (v0.4.0): auditoría de datos con Hibernate Envers
-- Ejecutar UNA vez en MySQL Workbench conectado como root (cruzma_app no puede crear tablas).
-- Los permisos SELECT, INSERT, UPDATE de cruzma_app ya alcanzan a estas tablas nuevas.
USE cruzma_ventas;

-- Auditoría de datos (Hibernate Envers): quién creó o modificó cada registro, cuándo y con qué valores.
-- revinfo guarda una fila por transacción; cada <tabla>_aud guarda la versión del registro en esa revisión.
-- revtype: 0 = creación, 1 = modificación, 2 = eliminación. password_hash NO se audita.
CREATE TABLE IF NOT EXISTS revinfo (
  id_revision INT AUTO_INCREMENT PRIMARY KEY,
  fecha DATETIME NOT NULL,
  usuario VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS cliente_aud (
  id_cliente INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  tipo_doc VARCHAR(3),
  num_doc VARCHAR(11),
  razon_social VARCHAR(150),
  direccion VARCHAR(200),
  telefono VARCHAR(15),
  email VARCHAR(100),
  PRIMARY KEY (id_cliente, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE TABLE IF NOT EXISTS producto_aud (
  id_producto INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  codigo VARCHAR(20),
  descripcion VARCHAR(200),
  unidad VARCHAR(10),
  tipo VARCHAR(10),
  precio_unitario DECIMAL(10,2),
  PRIMARY KEY (id_producto, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE TABLE IF NOT EXISTS usuario_aud (
  id_usuario INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  username VARCHAR(50),
  nombres VARCHAR(100),
  activo BOOLEAN,
  id_rol INT,
  PRIMARY KEY (id_usuario, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE TABLE IF NOT EXISTS cotizacion_aud (
  id_cotizacion INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  numero VARCHAR(15),
  fecha DATE,
  fecha_vencimiento DATE,
  estado VARCHAR(10),
  total DECIMAL(12,2),
  id_cliente INT,
  id_usuario INT,
  PRIMARY KEY (id_cotizacion, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE TABLE IF NOT EXISTS venta_aud (
  id_venta INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  fecha DATETIME,
  total DECIMAL(12,2),
  saldo DECIMAL(12,2),
  estado VARCHAR(10),
  id_cliente INT,
  id_usuario INT,
  id_cotizacion INT,
  PRIMARY KEY (id_venta, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE TABLE IF NOT EXISTS pago_aud (
  id_pago INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  id_venta INT,
  fecha DATETIME,
  monto DECIMAL(12,2),
  medio VARCHAR(15),
  PRIMARY KEY (id_pago, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE TABLE IF NOT EXISTS comprobante_aud (
  id_comprobante INT NOT NULL,
  rev INT NOT NULL,
  revtype TINYINT,
  id_venta INT,
  tipo VARCHAR(2),
  serie VARCHAR(4),
  correlativo INT,
  fecha_emision DATETIME,
  subtotal DECIMAL(12,2),
  igv DECIMAL(12,2),
  total DECIMAL(12,2),
  estado_sunat VARCHAR(10),
  hash_cdr VARCHAR(100),
  ruta_pdf VARCHAR(255),
  PRIMARY KEY (id_comprobante, rev),
  FOREIGN KEY (rev) REFERENCES revinfo(id_revision)
);

CREATE INDEX idx_revinfo_fecha ON revinfo(fecha);

-- Comprobación: deben aparecer revinfo y 7 tablas *_aud
SHOW TABLES LIKE '%aud%';
SHOW TABLES LIKE 'revinfo';
