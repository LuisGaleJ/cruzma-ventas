# Publicar Cruzma Ventas en Railway

Resultado: el sistema queda en `https://<nombre>.up.railway.app`, abierto desde cualquier navegador o celular,
con la base MySQL en Railway. Cada merge a `main` se publica solo.

```
Navegador → Servicio cruzma-ventas (Dockerfile, Java 17) → MySQL de Railway (red privada)
                          ↓
             api.decolecta.com · api.nubefact.com
```

| Plan | Costo |
|---|---|
| Prueba (Trial) | 30 días con US$5 de crédito, sin tarjeta |
| Hobby | US$5 al mes (incluye US$5 de consumo) |

> Railway construye la aplicación con el `Dockerfile` del proyecto: no hay que instalar nada ni cambiar el código.

---

## 0. Antes de empezar

Fusiona el PR de `feature/despliegue` en `main` (Railway publica desde `main`) y haz `git pull` en tu PC.

## 1. Crear la cuenta

1. Entra a <https://railway.com> → **Login** → **Login with GitHub** con la cuenta `LuisGaleJ`.
2. Acepta los términos. Debe quedar activo el **Trial** con US$5 de crédito.
3. Si Railway muestra *Limited trial* o pide verificar la cuenta, sigue lo que indica (suele bastar con la cuenta de GitHub).

## 2. Crear el proyecto con la base MySQL

1. **New Project** → **Deploy MySQL** (o **Database → Add MySQL**). Aparece el servicio **MySQL** en el lienzo.
2. Espera a que esté en verde (*Active*).

## 3. Cargar las tablas desde MySQL Workbench

1. Clic en el servicio **MySQL** → pestaña **Variables** (o botón **Connect** → *Public Network*).
   Ahí están el host público (algo como `xxxx.proxy.rlwy.net`), el puerto, el usuario `root` y la contraseña.
2. Workbench → **+** nueva conexión con esos datos (Hostname, Port, Username `root`, Password → *Store in Vault*).
3. Abre `db/schema.sql` (*File → Open SQL Script*) y ejecútalo completo. Crea la base `cruzma_ventas` con todas las tablas.
4. Abre `db/nube-usuario-app.sql`, reemplaza `ESCRIBE_AQUI_UNA_CLAVE` por una contraseña nueva y ejecútalo.
   **Ciérralo sin guardar** (no debe quedar la clave en el proyecto).
5. Comprueba: `SHOW TABLES FROM cruzma_ventas;`

> Los roles, el usuario `admin` y los datos de ejemplo los crea el sistema la primera vez que arranca.

## 4. Agregar la aplicación desde GitHub

1. En el lienzo del proyecto → **+ Create** (o **New**) → **GitHub Repo** → `LuisGaleJ/cruzma-ventas`.
   Si no aparece, **Configure GitHub App** y da acceso al repositorio.
2. Railway detecta el `Dockerfile` y empieza a construir. El primer intento puede fallar por falta de variables: es normal.

## 5. Variables de entorno de la aplicación

Clic en el servicio **cruzma-ventas** → pestaña **Variables** → **+ New Variable** (o **Raw Editor** para pegarlas juntas).

| Nombre | Valor | Notas |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `mysql` | Usa MySQL en lugar de H2 |
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/cruzma_ventas?allowPublicKeyRetrieval=true` | Escríbelo tal cual: Railway reemplaza `${{MySQL...}}` por la dirección privada de la base |
| `DB_USER` | `cruzma_app` | Usuario con mínimo privilegio |
| `DB_PASSWORD` | la del paso 3.4 | |
| `CRUZMA_PUBLICO` | `true` | Cookie solo por HTTPS y exige contraseña propia |
| `ADMIN_PASSWORD` | una contraseña nueva (10+ caracteres) | Clave inicial de `admin`, `vendedor` y `caja` |
| `DEMO_DATOS` | `true` | Carga clientes y productos de ejemplo |
| `TZ` | `America/Lima` | Fechas y horas de Perú |
| `OSE_PROVEEDOR` | `nubefact` | O `simulado` |
| `OSE_URL` | RUTA de tu cuenta de Nubefact | Solo si `OSE_PROVEEDOR=nubefact` |
| `OSE_TOKEN` | TOKEN de tu cuenta de Nubefact | Solo si `OSE_PROVEEDOR=nubefact` |
| `DECOLECTA_TOKEN` | token de Decolecta | Si no se pone, la búsqueda de DNI/RUC queda desactivada |

> `PORT` no se agrega: Railway lo define solo y el sistema lo lee (`server.port=${PORT:8080}`).
> Si el servicio de base de datos no se llama `MySQL`, cambia ese nombre dentro de `${{...}}`.

Al guardar, Railway vuelve a desplegar (**Deploy** o **Apply changes** si lo pide).

## 6. Dirección pública

Servicio **cruzma-ventas** → **Settings** → **Networking** → **Generate Domain**.
Si pregunta el puerto, pon `8080`. Queda una dirección `https://cruzma-ventas-production-xxxx.up.railway.app`
(en *Settings* puedes cambiar el nombre).

## 7. Probar y dar acceso al gerente

1. Abre la dirección. El primer arranque tarda 1–2 minutos.
2. Ingresa con `admin` y la contraseña de `ADMIN_PASSWORD`.
3. **Usuarios** → cambia la contraseña de `vendedor` y `caja` (o desactívalos).
4. **Usuarios → Nuevo usuario** para el gerente (rol **ADMIN** si debe ver todo).
5. Envíale el enlace y sus datos por un canal privado.

## 8. Publicación automática

Cada merge a `main` vuelve a construir y publicar (pestaña **Deployments** muestra el historial y los logs).
Si algún cambio falla, Railway mantiene en línea la versión anterior.

## 9. Si algo falla

Servicio → **Deployments** → el último → **View logs**.

| Mensaje en el log | Qué revisar |
|---|---|
| `IllegalStateException ... ADMIN_PASSWORD` | Falta `ADMIN_PASSWORD`, es la de demostración o tiene menos de 10 caracteres |
| `Communications link failure` | `DB_URL` mal escrita o el servicio MySQL tiene otro nombre |
| `Access denied for user 'cruzma_app'` | `DB_PASSWORD` distinta a la del paso 3.4, o no se ejecutó `nube-usuario-app.sql` |
| `Public Key Retrieval is not allowed` | Falta `?allowPublicKeyRetrieval=true` al final de `DB_URL` |
| `Table ... doesn't exist` | No se ejecutó `db/schema.sql` en la base de Railway |
| El servicio se reinicia solo (*OOM*, memoria) | Plan de prueba con poca RAM: pasar a Hobby o avisar para ajustar |

## 10. Cuidar el crédito y la seguridad

- **Usage** (en la cuenta) muestra cuánto se ha consumido del crédito.
- Terminada la carga de tablas, puedes desactivar la red pública de MySQL (servicio MySQL → *Settings → Networking*):
  la aplicación usa la red privada y no la necesita.
- Ninguna contraseña ni token va en el código ni en GitHub: todo está en las variables de Railway.
- Con la cuenta **demo** de Nubefact los comprobantes no llegan a SUNAT. No presionar «Activar con la SUNAT».
- Para dejarlo como portafolio gratuito después de la prueba, el mismo `Dockerfile` sirve en Render (aplicación) + Aiven (MySQL).
