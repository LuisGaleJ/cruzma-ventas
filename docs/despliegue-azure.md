# Publicar Cruzma Ventas en Azure (Azure for Students)

Resultado: el sistema queda en `https://<nombre>.azurewebsites.net`, abierto desde cualquier navegador o celular,
con la base MySQL en Azure. Cada merge a `main` se publica solo (GitHub Actions).

```
Navegador → App Service (Java 17, plan B1) → Azure Database for MySQL (B1ms)
                     ↓
          api.decolecta.com · api.nubefact.com
```

| Recurso | Plan | Costo con Azure for Students |
|---|---|---|
| Azure Database for MySQL – Flexible Server | Burstable **B1ms**, 32 GB | Gratis 12 meses (750 h al mes) |
| App Service (aplicación) | Linux **B1** | Se descuenta del crédito de US$100 |

> El plan gratuito de App Service (F1) no sirve para Spring Boot: se apaga y solo da 60 minutos de CPU al día.

---

## 1. Activar Azure for Students

1. Entra a <https://azure.microsoft.com/free/students> y elige **Start free**.
2. Regístrate con tu **correo UTP**. No pide tarjeta.
3. Al terminar, en <https://portal.azure.com> debe aparecer la suscripción **Azure for Students**.

## 2. Crear un grupo de recursos

Un grupo de recursos guarda juntos todos los servicios del proyecto (y permite borrarlos de una sola vez).

1. Buscador del portal → **Grupos de recursos** → **Crear**.
2. Suscripción: *Azure for Students*. Nombre: `rg-cruzma`. Región: **East US 2** (o la que el portal permita).
3. **Revisar y crear** → **Crear**.

> Si al crear algo aparece un error de **política** o de **región no permitida**, la suscripción de estudiante no admite esa
> región: repite con otra (por ejemplo *Brazil South*, *Central US* o *West US 2*) y usa **la misma región** para todo.

## 3. Crear la base MySQL

1. Buscador → **Azure Database for MySQL flexible servers** → **Crear** (servidor flexible).
2. Pestaña **Datos básicos**:
   - Grupo de recursos: `rg-cruzma`.
   - Nombre del servidor: por ejemplo `cruzma-mysql` (será `cruzma-mysql.mysql.database.azure.com`).
   - Región: la misma del grupo. Versión de MySQL: **8.0**.
   - Tipo de carga de trabajo: **Para proyectos de desarrollo o hobby**.
   - **Configurar servidor** → *Burstable* → **Standard_B1ms**, almacenamiento **20–32 GB**, alta disponibilidad **desactivada**.
   - Autenticación: **solo MySQL**. Usuario administrador: `cruzmaadmin`. Contraseña: una nueva, solo para esto.
3. Pestaña **Redes**:
   - Acceso **público**.
   - Marca **Permitir el acceso público desde cualquier servicio de Azure** (lo necesita la aplicación web).
   - **Agregar la dirección IP del cliente actual** (para conectarte desde tu PC con Workbench).
4. **Revisar y crear**. En el resumen de costo debe figurar el beneficio gratuito; luego **Crear** (tarda unos minutos).

## 4. Crear las tablas desde MySQL Workbench

1. Workbench → **+** nueva conexión:
   - Hostname: `cruzma-mysql.mysql.database.azure.com` · Port: `3306` · Username: `cruzmaadmin`.
   - Pestaña **SSL** → *Use SSL*: **Require**.
2. Conéctate y abre `db/schema.sql` (*File → Open SQL Script*) → ejecuta todo (rayo).
   Crea la base `cruzma_ventas` con todas las tablas, auditoría y descuentos.
3. Abre `db/nube-usuario-app.sql`, reemplaza `ESCRIBE_AQUI_UNA_CLAVE` por una contraseña nueva y ejecútalo.
   **Cierra el archivo sin guardar** (no debe quedar la clave escrita en el proyecto).
4. Opcional (horas de Perú en la base): servidor MySQL → **Configuración → Parámetros del servidor** → `time_zone` = `-05:00` → Guardar.
5. Comprueba: `SHOW TABLES FROM cruzma_ventas;` debe listar las tablas (`cliente`, `producto`, `revinfo`, `venta_aud`...).

> Los roles, el usuario `admin` y los datos de ejemplo **no** se cargan con SQL: los crea el sistema la primera vez que arranca.

## 5. Crear la aplicación web

1. Buscador → **App Services** → **Crear** → **Aplicación web**.
2. Pestaña **Datos básicos**:
   - Grupo de recursos: `rg-cruzma`.
   - Nombre: por ejemplo `cruzma-ventas` (si está ocupado, `cruzma-ventas-piura`). Será la dirección del sistema.
   - Publicar: **Código**. Pila del entorno: **Java 17**. Pila del servidor web: **Java SE (servidor web insertado)**.
   - Sistema operativo: **Linux**. Región: la misma.
   - Plan: crea uno nuevo → plan de precios **Básico B1**.
3. Pestaña **Implementación**: implementación continua **deshabilitada** (el proyecto ya trae su propio flujo de GitHub)
   y **Autenticación básica: Habilitar** (necesaria para el perfil de publicación).
4. **Revisar y crear** → **Crear**.

## 6. Variables de entorno de la aplicación

En la aplicación web → **Configuración → Variables de entorno** → pestaña **Configuración de la aplicación** → **+ Agregar**
cada una. Al final, **Aplicar** (la aplicación se reinicia).

| Nombre | Valor | Notas |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `mysql` | Usa MySQL en lugar de H2 |
| `SERVER_PORT` | `80` | Puerto que espera Azure para Java SE |
| `TZ` | `America/Lima` | Fechas y horas de Perú |
| `DB_URL` | `jdbc:mysql://cruzma-mysql.mysql.database.azure.com:3306/cruzma_ventas?sslMode=REQUIRED` | Con el nombre de tu servidor |
| `DB_USER` | `cruzma_app` | Usuario con mínimo privilegio |
| `DB_PASSWORD` | la del paso 4.3 | |
| `CRUZMA_PUBLICO` | `true` | Cookie solo por HTTPS y exige contraseña propia |
| `ADMIN_PASSWORD` | una contraseña nueva (10+ caracteres) | Clave inicial de `admin`, `vendedor` y `caja` |
| `DEMO_DATOS` | `true` | Carga clientes y productos de ejemplo (para la prueba del gerente) |
| `OSE_PROVEEDOR` | `nubefact` | O `simulado` si no quieres usar Nubefact |
| `OSE_URL` | RUTA de tu cuenta de Nubefact | Solo si `OSE_PROVEEDOR=nubefact` |
| `OSE_TOKEN` | TOKEN de tu cuenta de Nubefact | Solo si `OSE_PROVEEDOR=nubefact` |
| `DECOLECTA_TOKEN` | token de Decolecta | Si no se pone, la búsqueda de DNI/RUC queda desactivada |

Luego, en **Configuración → Configuración general**:

- **Siempre activado (Always On)**: **Activado** (para que no tarde al entrar después de un rato).
- **Solo HTTPS**: **Activado**.

## 7. Conectar GitHub con Azure

1. En la aplicación web → **Información general** → **Descargar perfil de publicación**. Se baja un archivo `.PublishSettings`.
2. En GitHub → repositorio `cruzma-ventas` → **Settings → Secrets and variables → Actions**:
   - Pestaña **Secrets** → **New repository secret**: nombre `AZURE_WEBAPP_PUBLISH_PROFILE`, valor: **todo el contenido**
     del archivo descargado (ábrelo con el Bloc de notas, copia y pega).
   - Pestaña **Variables** → **New repository variable**: nombre `AZURE_WEBAPP_NAME`, valor: el nombre de la aplicación
     (por ejemplo `cruzma-ventas`).
3. **Borra el archivo `.PublishSettings`** de tu PC: contiene una credencial.

## 8. Publicar

- Haz merge de la rama `feature/despliegue` a `main`: el flujo **Publicar en Azure** compila, prueba y publica.
- O en GitHub → **Actions → Publicar en Azure → Run workflow**.

Cuando el flujo termine en verde, abre `https://<nombre>.azurewebsites.net`. La primera vez tarda 1–2 minutos
(Spring Boot arranca y crea los datos iniciales).

## 9. Primeros pasos en el sistema publicado

1. Ingresa con `admin` y la contraseña de `ADMIN_PASSWORD`.
2. **Usuarios** → edita `vendedor` y `caja` y cámbiales la contraseña (o desactívalos si no los usarás).
3. **Usuarios → Nuevo usuario** para el gerente: su propio usuario y contraseña, rol **ADMIN** si debe ver todo.
4. Envíale el enlace y sus datos de acceso por un canal privado.

## 10. Si algo falla

| Síntoma | Qué revisar |
|---|---|
| *Application Error* o la página no carga | Aplicación web → **Supervisión → Secuencia de registro** (log en vivo) |
| `IllegalStateException ... ADMIN_PASSWORD` | Falta `ADMIN_PASSWORD` o es la de demostración o tiene menos de 10 caracteres |
| `Communications link failure` | `DB_URL` mal escrita o falta *Permitir acceso desde servicios de Azure* en Redes de MySQL |
| `Access denied for user 'cruzma_app'` | `DB_PASSWORD` distinta a la del paso 4.3, o no se ejecutó `nube-usuario-app.sql` |
| `Table ... doesn't exist` | No se ejecutó `db/schema.sql` en el servidor de Azure |
| El flujo de GitHub se omite | Falta la variable `AZURE_WEBAPP_NAME` |
| El flujo falla en *Publicar* | Secreto `AZURE_WEBAPP_PUBLISH_PROFILE` incompleto, o la autenticación básica está deshabilitada |

## 11. Cuidar el crédito

- **Administración de costos → Presupuestos**: crea un presupuesto de US$10 al mes con alerta a tu correo.
- Si no se usará por un tiempo, **Detener** la aplicación web y el servidor MySQL desde su página
  (el servidor MySQL detenido se vuelve a encender solo a los 30 días).
- Al terminar el curso, borrar el grupo `rg-cruzma` elimina todo.

## Seguridad

- Ninguna contraseña ni token va en el código ni en GitHub: todos son variables de entorno de Azure o secretos de GitHub.
- `CRUZMA_PUBLICO=true` impide arrancar con la contraseña de demostración del README.
- La conexión a MySQL va cifrada (`sslMode=REQUIRED`) y la aplicación usa `cruzma_app` (SELECT, INSERT, UPDATE), no el administrador.
- Con la cuenta **demo** de Nubefact los comprobantes no llegan a SUNAT. No presionar «Activar con la SUNAT».
