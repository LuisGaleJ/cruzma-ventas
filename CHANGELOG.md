# Historial de cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/) y versionado semántico.

## [Sin publicar]

### Cambiado
- P4 Clientes: botón **Nuevo cliente** y edición en una ventana emergente (modal de Bootstrap) en lugar del formulario lateral.
  Si hay errores de validación, la ventana se vuelve a abrir con los mensajes. Campos obligatorios marcados con asterisco rojo.
- P5 Productos y servicios: botón **Nuevo producto o servicio** y edición en ventana emergente (solo ADMIN), con columna Tipo en la tabla.
  Si falta un dato, la ventana se vuelve a abrir con lo escrito y el mensaje.
- Código automático de productos y servicios: `PRD-001`, `PRD-002`... y `SRV-001`, `SRV-002`... Lo asigna el servidor
  al registrar (el formulario lo muestra en solo lectura y cambia con el tipo); al editar se conserva, salvo que cambie el tipo.
- Tablas de Clientes y de Productos y servicios con **DataTables 2.1** (búsqueda instantánea, orden por columna
  y paginación de 10/25/50/100, en español). Configuración común en `static/js/tablas.js`.
- P3 Usuarios: botón **Nuevo usuario**, edición en ventana emergente (nombres, rol y contraseña opcional) y tabla con DataTables.
  El nombre de usuario no se edita y nadie puede cambiar su propio rol. El navegador ya no autocompleta el formulario con la sesión.
- **Cotización por medida**: si el producto es M, ML, M2 o M3, el subtotal es precio x cantidad x metrado.
  Las medidas se escriben como área o longitud (`5`) o como largo x alto (`3.20 x 0.90`); la pantalla muestra el metrado
  calculado y el servidor lo vuelve a calcular (clase `Metrado`). La venta conserva el importe de la cotización.
- Corregido: con una sola fila, una medida con coma (`3,20 x 0,90`) ya no se parte en dos valores.
- P7 Venta: **Registrar pago** en ventana emergente, con el saldo pendiente a la vista y el atajo «Pagar el saldo completo».
- El teléfono del cliente solo acepta números (6 a 9 dígitos): validado en el servidor y filtrado al escribir.

## [0.4.0] – APF3, auditoría de datos

### Agregado
- Control de cambios en la base de datos con **Hibernate Envers**: `@Audited` en Cliente, Producto, Usuario, Cotización, Venta, Pago y Comprobante.
- Tabla `revinfo` (revisión, fecha, usuario) y tablas `*_aud` con la versión de cada registro; `password_hash` no se audita.
- `RevisionUsuarioListener` guarda el usuario de la sesión en cada revisión.
- Pantalla **P9 Historial de cambios** (`/auditoria`, solo ADMIN) con filtros y detalle "campo: anterior → nuevo".
- Script `db/migracion-002-auditoria.sql` para bases MySQL existentes; `db/schema.sql` incluye las tablas nuevas.
- 6 pruebas nuevas (30 en total), entre ellas una prueba de integración con Spring Boot y H2.

## [0.3.1] – Conexión a MySQL

### Agregado
- Perfil `mysql` que lee la contraseña de la variable de entorno `DB_PASSWORD` y usa el usuario `cruzma_app`.

## [0.3.0] – APF3 (semana 12)

### Agregado
- Exportación de reportes a Excel (.xlsx) con **Apache POI**: 6 hojas (Resumen, Ventas, R1, R2 clientes, R2 productos, R3).
- Exportación CSV con escape estándar mediante **Apache Commons Text**.
- Interfaz `ExportadorReporte` para agregar formatos sin modificar el controlador (principio abierto/cerrado).
- Bloqueo del usuario tras 5 intentos fallidos durante 15 minutos con la caché de **Google Guava**.
- Utilidad `DatosSeguros` (**Apache Commons Lang**): normalizar textos, enmascarar DNI/RUC en logs y neutralizar fórmulas.
- Configuración de **Logback** (`logback-spring.xml`): log general rotativo y log de auditoría.
- Registro de auditoría de inicios de sesión, usuarios, conversiones a venta, comprobantes y exportaciones.
- Cabeceras de seguridad (frame-options, referrer-policy), sesión única por usuario y cookie de sesión `HttpOnly` y `SameSite=Strict`.
- 18 pruebas nuevas (24 en total) con JUnit 5 y Mockito.
- Integración continua con GitHub Actions.

### Cambiado
- Las validaciones del dominio usan `Preconditions` de Guava.
- `ReporteService.armar` reúne los datos de los reportes; el controlador ya no calcula totales.

## [0.2.0] – APF2, documentación

### Agregado
- Javadoc en todas las clases y métodos; `package-info.java` por paquete.
- Carpeta `docs/` con arquitectura, modelo de datos, reglas de negocio, rutas y guía del desarrollador.
- Datos de la empresa (RUC, nombre, dirección) configurables en `application.properties`.

## [0.1.0] – APF2, prototipo funcional

### Agregado
- Proyecto Spring Boot con arquitectura en capas (MVC, servicios, repositorios JPA).
- 8 pantallas: login, panel, usuarios, clientes, productos, cotizaciones, venta y comprobante, reportes.
- Emisión y anulación de comprobantes con OSE simulado; script `db/schema.sql` para MySQL.
