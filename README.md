# Cruzma Ventas

Prototipo funcional del sistema web de cotizaciones, ventas y facturación electrónica de **Grupo Cruzma** (Piura).
Proyecto del curso Integrador I: Sistemas Software – UTP. Autor: Galecio Jaramillo, Luis Osmin.

El sistema une en un solo flujo lo que hoy se hace en Excel, en cuaderno y en el portal SOL:
**cotización → venta → pago → comprobante electrónico → reportes**.

## Tecnologías

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3 (Web, Security, Data JPA, Validation) |
| Vistas | Thymeleaf + Bootstrap 5.3 + CSS propio (`static/css/cruzma.css`) |
| Base de datos | H2 en memoria (demo) o MySQL 8 (perfil `mysql`) |
| Facturación electrónica | Interfaz `ServicioFacturacionOSE`; en el prototipo, `OseSimuladoService` |
| Librerías de apoyo | Google Guava, Apache POI, Apache Commons Lang y Text, Logback |
| Auditoría de datos | Hibernate Envers (historial de cambios en tablas `*_aud`) |
| Pruebas | JUnit 5, Mockito y Spring Boot Test (30 pruebas) |
| Build | Maven |

Composición del código: clases Java organizadas en 9 paquetes, 15 vistas HTML, 1 hoja CSS y 2 scripts SQL. El Java es más del 60 % del código fuente.

## Cómo ejecutarlo

Requisitos: JDK 17 o superior y Maven 3.9.

```bash
mvn spring-boot:run
```

Abrir http://localhost:8080. Usuarios de demostración (contraseña `Cruzma2026` en todos):

| Usuario | Rol | Qué puede hacer |
|---------|-----|-----------------|
| admin | ADMIN | Todo: usuarios, catálogo, reportes completos |
| vendedor | VENDEDOR | Clientes, cotizaciones, convertir en venta, reportes R2 |
| caja | CAJA | Pagos, emitir y anular comprobantes, reporte R3 |

### Desde NetBeans

Abrir con *File → Open Project* la carpeta `cruzma-ventas` (NetBeans la reconoce como proyecto Maven).
El archivo `nbactions.xml` hace que **Run** (F6) ejecute `spring-boot:run`; clic derecho → *Run Maven → Run con MySQL* usa el perfil `mysql`.

### Con MySQL

```bash
mysql -u root -p < db/schema.sql
export DB_URL=jdbc:mysql://localhost:3306/cruzma_ventas DB_USER=cruzma_app DB_PASSWORD=CambiarEstaClave2026
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Variables del proveedor de facturación: `OSE_URL` y `OSE_TOKEN` (nunca dentro del código).

## Arquitectura en capas

```
src/main/java/pe/cruzma/ventas
├── CruzmaVentasApplication.java   Punto de entrada
├── config/        SecurityConfig (roles, BCrypt) y DataInitializer (datos demo)
├── controller/    Controladores MVC, uno por pantalla (P1 a P8)
├── service/       Reglas de negocio y transacciones
├── repository/    Interfaces Spring Data JPA
├── model/         Entidades JPA y enumeraciones (mapean db/schema.sql)
├── integration/   ServicioFacturacionOSE, RespuestaOSE y OseSimuladoService
└── dto/           ItemCotizacionForm, FilaReporte, ResumenPanel
src/main/resources
├── templates/     Vistas Thymeleaf
├── static/css/    Estilos
└── application*.properties
db/schema.sql      Diseño físico en MySQL
```

Flujo de una petición: `Controller` recibe el formulario → `Service` aplica la regla y abre la transacción → `Repository` persiste → la vista Thymeleaf muestra el resultado o el mensaje de error.

## Pantallas y rutas

| Pantalla | Ruta | Método | Roles | Requisitos |
|----------|------|--------|-------|------------|
| P1 Inicio de sesión | `/login` | GET/POST | Todos | RF01 |
| P2 Panel | `/` | GET | Todos | RF12, RF13 |
| P3 Usuarios | `/usuarios`, `/usuarios/{id}/estado` | GET/POST | ADMIN | RF02 |
| P4 Clientes | `/clientes`, `/clientes/guardar` | GET/POST | Todos | RF03 |
| P5 Productos | `/productos`, `/productos/guardar` | GET/POST | Consulta todos, edita ADMIN | RF04 |
| P6 Cotizaciones | `/cotizaciones`, `/cotizaciones/nueva`, `/cotizaciones/{id}` | GET/POST | ADMIN, VENDEDOR | RF05, RF06 |
| Convertir en venta | `/cotizaciones/{id}/convertir` | POST | ADMIN, VENDEDOR | RF07 |
| P7 Venta y comprobante | `/ventas/{id}`, `/ventas/{id}/pagos`, `/ventas/{id}/emitir` | GET/POST | Pagos y emisión: ADMIN, CAJA | RF08, RF09, RF11 |
| Comprobantes | `/comprobantes`, `/comprobantes/{id}`, `/comprobantes/{id}/anular` | GET/POST | Anular: ADMIN, CAJA | RF09, RF10 |
| P8 Reportes | `/reportes`, `/reportes/exportar?formato=xlsx\|csv` | GET | Secciones según rol | RF12, RF13, RF14 |
| P9 Historial de cambios | `/auditoria?entidad=&id=` | GET | ADMIN | Auditoría de datos |

## Reglas de negocio implementadas

- Los precios del catálogo **incluyen IGV**. El comprobante separa: `op. gravada = total / 1.18` e `IGV = total − op. gravada` (clase `Comprobante`).
- Cotización válida **15 días** (`CotizacionService.DIAS_VALIDEZ`); al vencer pasa a `VENCIDA` y no se puede convertir.
- Una cotización genera **como máximo una venta** (`venta.id_cotizacion` es único).
- Un pago no puede superar el saldo; cuando el saldo llega a 0 la venta pasa a `PAGADA` (`Venta.registrarPago`).
- Para emitir un comprobante la venta debe tener **al menos un pago** y ningún comprobante aceptado (CU08).
- **Factura (01)** solo para clientes con RUC; **boleta (03)** para DNI. Series `F001`, `B001`; nota de crédito `FC01`.
- El correlativo es el último de la serie + 1; `serie + correlativo` es único.
- Un comprobante aceptado **no se borra**: se anula con nota de crédito (07), queda `ANULADO` y la venta `ANULADA`.
- DNI de 8 dígitos; RUC de 11 que empieza en 10, 15, 17 o 20 (`Cliente.documentoValido`).

## Seguridad

| Medida | Dónde |
|--------|-------|
| Contraseñas con BCrypt | `SecurityConfig.passwordEncoder`, `UsuarioService.crear` |
| Acceso por rol en rutas | `SecurityConfig.filterChain` |
| Menús y botones según rol | `sec:authorize` en las vistas |
| Protección CSRF en formularios | Spring Security + `th:action` |
| Consultas parametrizadas (sin inyección SQL) | Spring Data JPA y `@Query` con `@Param` |
| Usuarios desactivados no ingresan | `UsuarioDetailsService` |
| Credenciales del OSE fuera del código | `OSE_URL`, `OSE_TOKEN` |
| Usuario de BD con mínimo privilegio | `db/schema.sql` (`cruzma_app`: SELECT, INSERT, UPDATE) |
| Historial de cambios en la BD (quién, cuándo, qué) | Hibernate Envers: `@Audited`, `revinfo`, `*_aud`, pantalla `/auditoria` |

## Integración con SUNAT

El sistema depende de la interfaz `ServicioFacturacionOSE` (métodos `enviar` y `comunicarBaja`).
En el prototipo, `OseSimuladoService` valida como lo haría el OSE (factura con RUC, documento válido, total > 0)
y devuelve un CDR simulado. Para producción se crea otra implementación que llame a la API REST del proveedor
elegido (por ejemplo Nubefact) con el XML UBL 2.1, sin cambiar controladores ni servicios.

## Pruebas

```bash
mvn test
```

30 pruebas en 9 clases: reglas del dominio, servicios de cotización y comprobantes (con Mockito),
bloqueo de login, tratamiento seguro de datos, exportación a Excel y CSV, y auditoría de datos
(incluida una prueba de integración con Spring Boot y H2 que verifica el historial de Envers).

## Librerías de apoyo (APF3)

| Librería | Para qué se usa | Dónde |
|----------|-----------------|-------|
| Google Guava | `Preconditions` en las validaciones del dominio, listas inmutables en los reportes, `Maps.uniqueIndex` para registrar los exportadores y `Cache` con expiración para bloquear el login tras 5 intentos fallidos | `Pago`, `DetalleCotizacion`, `ReporteService`, `ReporteController`, `IntentosLoginService` |
| Apache POI | Exportar los reportes R1, R2 y R3 a Excel (.xlsx) con 6 hojas y montos numéricos | `ExcelExportador` |
| Apache Commons Lang | Normalizar textos, enmascarar DNI/RUC en los logs y detectar fórmulas peligrosas | `DatosSeguros`, `ClienteService` |
| Apache Commons Text | Escapar valores del CSV (RFC 4180) | `CsvExportador` |
| Logback | Log general rotativo (`logs/cruzma-ventas.log`, 30 días) y log de auditoría (`logs/auditoria.log`, 90 días) | `logback-spring.xml`, `Auditoria` |

## Pendiente para la versión final

- Implementación real del OSE (XML UBL 2.1, firma digital, código QR en el PDF).
- PDF generado en servidor con JasperReports (hoy se usa la vista imprimible del navegador).

## Documentación

- **APF3**: [`docs/apf3-cumplimiento.md`](docs/apf3-cumplimiento.md) explica cómo el proyecto cumple cada criterio de la rúbrica.
- **Auditoría de datos**: [`docs/auditoria-de-datos.md`](docs/auditoria-de-datos.md).
- **Git y GitHub**: [`docs/git-y-github.md`](docs/git-y-github.md).
- **Cambios por versión**: [`CHANGELOG.md`](CHANGELOG.md).

- **Código**: Javadoc en todas las clases, constructores y métodos públicos, más un `package-info.java` por paquete.
  Se revisa con `doclint` (cero advertencias). Generar: `mvn javadoc:javadoc` → `target/site/apidocs/index.html`.
  Copia ya generada: [`docs/javadoc/index.html`](docs/javadoc/index.html).
- **Técnica**: carpeta [`docs/`](docs/README.md) con arquitectura, modelo de datos, reglas de negocio, rutas y guía del desarrollador.

## Convenciones

- Clases en PascalCase; métodos y variables en camelCase; tablas y columnas en snake_case.
- Ramas: `main`, `develop`, `feature/<modulo>`. Commits: `feat:`, `fix:`, `docs:`.
