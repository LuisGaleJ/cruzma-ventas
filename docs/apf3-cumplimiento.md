# Avance de Proyecto Final 3 – Cumplimiento de la rúbrica

Este documento relaciona cada criterio de la rúbrica del APF3 con lo que está construido en el código, para que se pueda verificar abriendo las clases indicadas.

## 1. Diseño de la solución: arquitectura, principios y seguridad

La rúbrica pide al menos 3 de MVC, TDD, DAO y SOLID. El proyecto aplica **los cuatro**.

### MVC (Modelo – Vista – Controlador)

| Parte | En el proyecto | Ejemplo |
|-------|----------------|---------|
| Modelo | Entidades JPA en `model/` y reglas en `service/` | `Venta`, `Comprobante`, `ComprobanteService` |
| Vista | Plantillas Thymeleaf en `templates/` | `ventas/detalle.html`, `reportes/index.html` |
| Controlador | Un controlador por pantalla en `controller/` | `VentaController.emitir` recibe el formulario, llama al servicio y elige la vista |

### DAO (objeto de acceso a datos)

Cada tabla tiene su DAO en `repository/` (interfaces de Spring Data JPA). Los servicios nunca escriben SQL: piden los datos al DAO.
Las consultas propias (`VentaRepository.totalVendido`, `ComprobanteRepository.ultimoCorrelativo`) usan parámetros, sin concatenar texto.

### SOLID

| Principio | Cómo se aplica | Dónde |
|-----------|----------------|-------|
| **S** – Responsabilidad única | Cada clase hace una cosa: el controlador atiende la web, el servicio aplica la regla, el DAO guarda, el exportador genera el archivo | `ReporteController`, `ReporteService`, `ExcelExportador` |
| **O** – Abierto/cerrado | Para agregar un formato de exportación (por ejemplo PDF) se crea una clase nueva que implementa `ExportadorReporte`; el controlador no se modifica | `ExportadorReporte`, `ExcelExportador`, `CsvExportador` |
| **L** – Sustitución de Liskov | `OseSimuladoService` se puede reemplazar por un proveedor real que implemente `ServicioFacturacionOSE` sin cambiar `ComprobanteService` | `integration/` |
| **I** – Segregación de interfaces | Interfaces pequeñas y específicas: un repositorio por entidad, `ServicioFacturacionOSE` con solo dos métodos | `repository/`, `ServicioFacturacionOSE` |
| **D** – Inversión de dependencias | Los servicios dependen de interfaces y las reciben por constructor | `ComprobanteService(…, ServicioFacturacionOSE ose, …)` |

### TDD (desarrollo guiado por pruebas)

24 pruebas automatizadas en 6 clases, ejecutadas con `mvn test` y en cada push a GitHub. Las reglas se desarrollan con el ciclo
rojo (prueba que falla) → verde (código mínimo) → refactorizar. Los servicios se prueban aislados con Mockito, simulando los DAO y el OSE.

| Prueba | Regla que protege |
|--------|-------------------|
| `ComprobanteServiceTest.noEmiteFacturaAClienteConDni` | Factura solo con RUC (SUNAT) |
| `ComprobanteServiceTest.noEmiteSinPagoRegistrado` | Precondición del CU08 |
| `ComprobanteServiceTest.emiteBoletaConElSiguienteCorrelativo` | Serie, correlativo e IGV |
| `CotizacionServiceTest.noConvierteCotizacionVencida` | Validez de 15 días |
| `IntentosLoginServiceTest.bloqueaAlLlegarAlMaximoDeIntentos` | Protección contra fuerza bruta |
| `ExportadoresTest.excelTieneSeisHojasYMontosNumericos` | RF14 y prevención de inyección de fórmulas |

La lista completa está en `docs/guia-del-desarrollador.md`.

### Seguridad

| Riesgo | Control | Dónde |
|--------|---------|-------|
| Contraseñas robadas de la base de datos | Hash BCrypt, nunca texto plano | `SecurityConfig`, `UsuarioService` |
| Acceso a módulos ajenos al rol | Autorización por ruta y menús según rol | `SecurityConfig`, `sec:authorize` |
| Fuerza bruta en el login | Bloqueo de 15 minutos tras 5 fallos (Guava Cache) | `IntentosLoginService` |
| Inyección SQL | Consultas parametrizadas JPA | `repository/` |
| CSRF | Token en todos los formularios | Spring Security + `th:action` |
| Robo de sesión | Cookie `HttpOnly` y `SameSite=Strict`, sesión de 30 min, una sesión por usuario | `application.properties`, `SecurityConfig` |
| Clickjacking | Cabecera `X-Frame-Options: DENY` | `SecurityConfig` |
| Inyección de fórmulas al exportar | Textos con `= + - @` se exportan como texto | `DatosSeguros`, exportadores |
| Datos personales en los logs | DNI/RUC enmascarados (`4587****`) | `DatosSeguros.enmascararDocumento` |
| Trazabilidad | Log de auditoría de login, usuarios, ventas, comprobantes y exportaciones | `Auditoria`, `logs/auditoria.log` |
| Cambios no rastreables en los datos | Historial en BD de quién creó o modificó cada registro, cuándo y qué valores cambiaron (Hibernate Envers); la app no puede borrarlo | `@Audited`, `revinfo`, `*_aud`, pantalla `/auditoria` |
| Errores que revelan el código | Sin trazas de error al usuario | `server.error.include-stacktrace=never` |
| Credenciales en el código | Variables de entorno `OSE_TOKEN`, `DB_PASSWORD` | `application*.properties` |

## 2. Uso de recursos Java (librerías de apoyo)

| Librería | Versión | Uso concreto | Consideración de seguridad |
|----------|---------|--------------|----------------------------|
| Google Guava | 33.3.1 | `Preconditions.checkArgument` en `Pago` y `DetalleCotizacion`; `ImmutableList` en `ReporteService.armar`; `Maps.uniqueIndex` para registrar exportadores; `Cache` con expiración en `IntentosLoginService` | Bloqueo de fuerza bruta; reportes inmutables que nadie altera después de calcularlos |
| Apache POI | 5.3.0 | `ExcelExportador` genera el .xlsx con 6 hojas, estilos y montos numéricos | Celdas peligrosas marcadas como texto (`setQuotePrefixed`) |
| Apache Commons Lang | 3.x | `StringUtils` en `DatosSeguros`: normalizar espacios, enmascarar documentos, detectar fórmulas | Datos personales enmascarados en logs |
| Apache Commons Text | 1.12.0 | `StringEscapeUtils.escapeCsv` en `CsvExportador` | Una coma o comilla en un nombre no rompe ni inyecta columnas |
| Logback | 1.5.x | `logback-spring.xml`: consola, log general rotativo (30 días) y log de auditoría (90 días) | No se registran contraseñas; auditoría separada |

## 3. Control de versiones

Git con ramas `main`/`develop`/`feature/*`, commits con Conventional Commits, etiquetas por entregable, `CHANGELOG.md`
e integración continua en GitHub Actions (`.github/workflows/ci.yml`). Procedimiento en `docs/git-y-github.md`.

Repositorio: `https://github.com/[usuario]/cruzma-ventas` *(completar con el enlace real)*.

## 4. Interfaces gráficas: cobertura del alcance

| Requisito | Pantalla | Estado |
|-----------|----------|--------|
| RF01 Iniciar sesión | P1 Login | Implementado, con bloqueo por intentos |
| RF02 Gestionar usuarios y roles | P3 Usuarios | Implementado |
| RF03 Clientes | P4 Clientes | Implementado |
| RF04 Catálogo | P5 Productos y servicios | Implementado |
| RF05 Cotizaciones con medidas y descuentos | P6 Nueva cotización | Implementado |
| RF06 Cotización en PDF | Detalle de cotización (imprimible) | Implementado |
| RF07 Convertir en venta | P6 → P7 | Implementado |
| RF08 Adelantos y saldo | P7 Venta | Implementado |
| RF09 Emitir boleta o factura | P7 Venta | Implementado (OSE simulado) |
| RF10 Anular con nota de crédito | P7 Venta | Implementado |
| RF11 Estado SUNAT | P7 y Comprobantes | Implementado |
| RF12 Reportes por período, cliente y producto | P8 Reportes | Implementado |
| RF13 Cuentas por cobrar | P2 Panel y P8 Reportes | Implementado |
| RF14 Exportar a PDF y Excel | P8 Reportes | Implementado: .xlsx (POI), CSV y vista imprimible |

Los 14 requisitos funcionales del SRS tienen pantalla funcionando. Además, **P9 Historial de cambios** (`/auditoria`, ADMIN) muestra el control de cambios de la base de datos; ver [auditoria-de-datos.md](auditoria-de-datos.md).

## 5. Producto final

| Criterio | Evidencia |
|----------|-----------|
| Completo | Tabla anterior: 14 de 14 requisitos funcionales |
| Coherente | Las clases, tablas y rutas coinciden con los diagramas del APF2; `docs/` y el Javadoc se generan desde el mismo código |
| Buenas prácticas | Arquitectura en capas, MVC, DAO, SOLID, pruebas, librerías estándar, Git, CI y Javadoc sin advertencias |
| Autoría y dominio | Cada clase indica el requisito que cumple; la sustentación recorre el flujo emitir comprobante de la vista al DAO |
