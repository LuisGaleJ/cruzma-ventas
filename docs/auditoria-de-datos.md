# Auditoría de datos (control de cambios en la base de datos)

Desde la versión 0.4.0 el sistema guarda **quién creó o modificó cada registro, cuándo y qué valores cambiaron**.
Se implementa con **Hibernate Envers**, el módulo de auditoría oficial de Hibernate, sin triggers ni código repetido en los servicios.

## Qué se audita

| Entidad | Tabla de historial | Campos que no se auditan |
|---------|--------------------|--------------------------|
| `Cliente` | `cliente_aud` | — |
| `Producto` | `producto_aud` | — |
| `Usuario` | `usuario_aud` | `password_hash` (nunca se guarda un historial de contraseñas) |
| `Cotizacion` | `cotizacion_aud` | lista de detalles |
| `Venta` | `venta_aud` | lista de detalles |
| `Pago` | `pago_aud` | — |
| `Comprobante` | `comprobante_aud` | — |

`Rol` y los detalles de cotización y venta no se auditan: los roles son fijos y los detalles no cambian después de grabarse.

## Cómo funciona

1. Cada entidad auditada tiene la anotación `@Audited`.
2. Cuando una transacción crea, modifica o elimina una de ellas, Envers inserta **una fila en `revinfo`**
   (número de revisión, fecha y hora, usuario) y **una fila por registro afectado en `<tabla>_aud`**
   con todos sus valores en ese momento y el tipo de operación (`revtype`: 0 creación, 1 modificación, 2 eliminación).
3. `RevisionUsuarioListener` completa el usuario con el que tiene la sesión abierta (`Auditoria.usuarioActual()`);
   los procesos internos, como la carga de datos de demostración, quedan como `sistema`.
4. El historial se escribe **en la misma transacción** que el cambio: si la operación falla, tampoco queda historial.

```
usuario edita un producto ──► ProductoService.guardar ──► UPDATE producto
                                                      └─► INSERT revinfo (id, fecha, usuario)
                                                      └─► INSERT producto_aud (id_producto, rev, revtype=1, valores...)
```

## Pantalla P9 Historial de cambios

Ruta `/auditoria` (solo ADMIN, menú **Historial de cambios**). Muestra, del más reciente al más antiguo,
la revisión, fecha y hora, usuario, registro, operación y el detalle `campo: valor anterior → valor nuevo`.
Se puede filtrar por tipo de registro y por id; al hacer clic en un registro se ve toda su historia.

| Clase | Capa | Responsabilidad |
|-------|------|-----------------|
| `model.Revision` | Modelo | Entidad de revisión (`revinfo`) con `@RevisionEntity` |
| `model.RevisionUsuarioListener` | Modelo | Guarda el usuario de la sesión en cada revisión |
| `service.HistorialCambiosService` | Servicio | Consulta las revisiones con `AuditReader` de Envers |
| `service.CamposAuditados` | Servicio | Catálogo de entidades y comparación entre versiones (sin acceso a BD) |
| `controller.AuditoriaController` | Controlador | `GET /auditoria?entidad=&id=` |
| `templates/auditoria/historial.html` | Vista | Tabla del historial y filtros |

## Base de datos

- H2 (perfil por defecto): Hibernate crea las tablas solo.
- MySQL (perfil `mysql`, `ddl-auto=none`): ejecutar **una vez** `db/migracion-002-auditoria.sql` en MySQL Workbench
  conectado como `root`. Los permisos de `cruzma_app` (SELECT, INSERT, UPDATE) ya alcanzan a las tablas nuevas,
  así que la aplicación puede escribir el historial pero **no puede borrarlo** (no tiene DELETE): el historial es inalterable desde la aplicación.
- Una base nueva se crea completa con `db/schema.sql`, que ya incluye estas tablas.

Consultas útiles en Workbench:

```sql
-- Últimos 20 cambios con su autor
SELECT r.id_revision, r.fecha, r.usuario, p.id_producto, p.revtype, p.precio_unitario
FROM producto_aud p JOIN revinfo r ON r.id_revision = p.rev
ORDER BY r.id_revision DESC LIMIT 20;

-- ¿Quién cambió el estado de un comprobante?
SELECT r.fecha, r.usuario, c.estado_sunat FROM comprobante_aud c
JOIN revinfo r ON r.id_revision = c.rev WHERE c.id_comprobante = 1 ORDER BY r.id_revision;
```

## Configuración

En `application.properties`:

```properties
spring.jpa.properties.org.hibernate.envers.audit_table_suffix=_aud
spring.jpa.properties.org.hibernate.envers.revision_field_name=rev
spring.jpa.properties.org.hibernate.envers.revision_type_field_name=revtype
spring.jpa.properties.org.hibernate.envers.store_data_at_delete=true
```

## Pruebas

| Prueba | Qué comprueba |
|--------|---------------|
| `CamposAuditadosTest` | Creación lista valores iniciales; modificación muestra solo lo que cambió; `100.00` y `100.0` no cuentan como cambio |
| `RevisionUsuarioListenerTest` | La revisión guarda el usuario de la sesión, o `sistema` sin sesión |
| `AuditoriaDatosIntegracionTest` | Con Spring Boot y H2: crear y modificar un producto deja dos revisiones con usuario, fecha y detalle correctos |

## Diferencia con el log de auditoría

| | Log `logs/auditoria.log` (Logback, v0.3.0) | Auditoría de datos (Envers, v0.4.0) |
|---|---|---|
| Registra | Acciones: inicio de sesión, emisión, exportación | Valores de los registros antes y después |
| Dónde | Archivo de texto rotativo | Tablas de la base de datos |
| Responde | ¿Quién intentó entrar? ¿Quién exportó? | ¿Quién cambió este precio, cuándo y de cuánto a cuánto? |
