# Guía del desarrollador

## Entorno

JDK 17 o superior, Maven 3.9 y, opcionalmente, MySQL 8. El IDE recomendado es NetBeans o IntelliJ; ambos abren el proyecto como Maven.

| Tarea | Comando |
|-------|---------|
| Ejecutar con H2 | `mvn spring-boot:run` |
| Ejecutar con MySQL | `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` |
| Pruebas unitarias | `mvn test` |
| Generar Javadoc | `mvn javadoc:javadoc` → `target/site/apidocs/index.html` |
| Empaquetar | `mvn package` → `target/cruzma-ventas-0.1.0.jar` |

## Convenciones de código

- Clases en `PascalCase`, métodos y variables en `camelCase`, constantes en `MAYUSCULAS`, tablas y columnas en `snake_case`.
- Nombres en español, igual que el dominio del negocio (cotización, comprobante, saldo).
- Dinero siempre en `BigDecimal` con escala 2 y redondeo `HALF_UP`.
- Inyección de dependencias por constructor; nunca `@Autowired` en atributos.
- Configuración (RUC, IGV, credenciales) en `application.properties` o variables de entorno, nunca escrita en el código.

## Cómo documentar

Toda clase, constructor y método público lleva Javadoc. Plantilla:

```java
/**
 * Qué hace, en una frase, y el requisito que cumple (RF08).
 * <p>
 * Detalle de la regla de negocio si no es evidente.
 *
 * @param idVenta venta que se cobra
 * @param monto   monto pagado, mayor a cero
 * @return la venta actualizada
 * @throws IllegalArgumentException si el monto supera el saldo
 */
```

Getters y setters usan la forma corta `/** {@return saldo pendiente de cobro} */`. Antes de entregar, `mvn javadoc:javadoc`
debe terminar sin advertencias (`doclint` está activado en el `pom.xml`).

## Agregar un módulo

Ejemplo: un módulo de proveedores.

1. **Tabla**: agregar `CREATE TABLE proveedor` en `db/schema.sql`.
2. **Entidad**: `model/Proveedor.java` con `@Entity`, validaciones y Javadoc.
3. **Repositorio**: `repository/ProveedorRepository extends JpaRepository<Proveedor, Long>`.
4. **Servicio**: `service/ProveedorService` con las reglas y `@Transactional` en los métodos que escriben.
5. **Controlador**: `controller/ProveedorController` con `@RequestMapping("/proveedores")`.
6. **Vista**: `templates/proveedores/lista.html`, reutilizando `fragments/layout`.
7. **Seguridad**: agregar la ruta y sus roles en `SecurityConfig` y el enlace en el menú con `sec:authorize`.
8. **Documentación**: Javadoc, una fila en `rutas-y-pantallas.md` y, si hay reglas nuevas, en `reglas-de-negocio.md`.

## Conectar el OSE real

1. Crear `integration/NubefactService implements ServicioFacturacionOSE` (o el proveedor elegido).
2. En `enviar`, armar el JSON o XML UBL 2.1 que pide el proveedor con los datos del `Comprobante` y llamar a su API con `RestClient`,
   usando `cruzma.ose.url` y `cruzma.ose.token` (variables `OSE_URL` y `OSE_TOKEN`).
3. Anotar `OseSimuladoService` con `@Profile("dev")` y la nueva clase con `@Profile("!dev")` para que Spring use una u otra.
4. Probar primero en el ambiente beta del proveedor. No hace falta tocar `ComprobanteService` ni los controladores.

## Pruebas

Se trabaja con TDD (desarrollo guiado por pruebas): rojo (escribir la prueba de la regla y ver que falla), verde (el código mínimo
para pasarla) y refactorizar con la prueba como red de seguridad.

| Clase de prueba | Qué prueba | Técnica |
|-----------------|------------|---------|
| `ReglasNegocioTest` | Totales, saldo, IGV, DNI/RUC | JUnit, sin base de datos |
| `CotizacionServiceTest` | Crear cotización, convertir en venta, vencida, doble conversión | Mockito simula los repositorios |
| `ComprobanteServiceTest` | Emitir boleta, factura con DNI, sin pago, rechazo del OSE, anulación | Mockito simula repositorios y OSE |
| `IntentosLoginServiceTest` | Bloqueo y desbloqueo por tiempo | Reloj falso de Guava (`Ticker`) |
| `DatosSegurosTest` | Normalizar, enmascarar, neutralizar fórmulas | JUnit |
| `ExportadoresTest` | Excel de 6 hojas con números; CSV escapado | Lee el .xlsx generado con POI |
