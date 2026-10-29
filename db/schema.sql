-- Diseño físico de la base de datos (sección 5.6 del informe)
-- Uso: mysql -u root -p < db/schema.sql
CREATE DATABASE IF NOT EXISTS cruzma_ventas CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE cruzma_ventas;

CREATE TABLE rol (
  id_rol INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nombres VARCHAR(100) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  id_rol INT NOT NULL,
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

CREATE TABLE cliente (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  tipo_doc ENUM('DNI','RUC') NOT NULL,
  num_doc VARCHAR(11) NOT NULL UNIQUE,
  razon_social VARCHAR(150) NOT NULL,
  direccion VARCHAR(200),
  telefono VARCHAR(15),
  email VARCHAR(100)
);

CREATE TABLE producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(20) NOT NULL UNIQUE,
  descripcion VARCHAR(200) NOT NULL,
  unidad VARCHAR(10) NOT NULL,
  tipo ENUM('PRODUCTO','SERVICIO') NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0)
);

CREATE TABLE cotizacion (
  id_cotizacion INT AUTO_INCREMENT PRIMARY KEY,
  numero VARCHAR(15) NOT NULL UNIQUE,
  fecha DATE NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  estado ENUM('PENDIENTE','APROBADA','RECHAZADA','VENCIDA') NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  id_cliente INT NOT NULL,
  id_usuario INT NOT NULL,
  FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE detalle_cotizacion (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_cotizacion INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  medidas VARCHAR(100),
  precio DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_cotizacion) REFERENCES cotizacion(id_cotizacion) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

CREATE TABLE venta (
  id_venta INT AUTO_INCREMENT PRIMARY KEY,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total DECIMAL(12,2) NOT NULL,
  saldo DECIMAL(12,2) NOT NULL,
  estado ENUM('PENDIENTE','PAGADA','ANULADA') NOT NULL,
  id_cliente INT NOT NULL,
  id_usuario INT NOT NULL,
  id_cotizacion INT NULL UNIQUE,
  FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_cotizacion) REFERENCES cotizacion(id_cotizacion)
);

CREATE TABLE detalle_venta (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  precio DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_venta) REFERENCES venta(id_venta) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

CREATE TABLE pago (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  monto DECIMAL(12,2) NOT NULL CHECK (monto > 0),
  medio ENUM('EFECTIVO','TRANSFERENCIA','YAPE','PLIN','TARJETA') NOT NULL,
  FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
);

CREATE TABLE comprobante (
  id_comprobante INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL,
  tipo ENUM('01','03','07') NOT NULL COMMENT '01 factura, 03 boleta, 07 nota de crédito',
  serie VARCHAR(4) NOT NULL,
  correlativo INT NOT NULL,
  fecha_emision DATETIME NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  igv DECIMAL(12,2) NOT NULL,
  total DECIMAL(12,2) NOT NULL,
  estado_sunat ENUM('PENDIENTE','ACEPTADO','RECHAZADO','ANULADO') NOT NULL,
  hash_cdr VARCHAR(100),
  ruta_pdf VARCHAR(255),
  UNIQUE (serie, correlativo),
  FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
);

CREATE INDEX idx_venta_fecha ON venta(fecha);
CREATE INDEX idx_comprobante_fecha ON comprobante(fecha_emision);

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

-- Usuario de aplicación con mínimo privilegio (sección 5.7)
CREATE USER IF NOT EXISTS 'cruzma_app'@'localhost' IDENTIFIED BY 'CambiarEstaClave2026';
GRANT SELECT, INSERT, UPDATE ON cruzma_ventas.* TO 'cruzma_app'@'localhost';
