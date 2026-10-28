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

-- Usuario de aplicación con mínimo privilegio (sección 5.7)
CREATE USER IF NOT EXISTS 'cruzma_app'@'localhost' IDENTIFIED BY 'CambiarEstaClave2026';
GRANT SELECT, INSERT, UPDATE ON cruzma_ventas.* TO 'cruzma_app'@'localhost';
