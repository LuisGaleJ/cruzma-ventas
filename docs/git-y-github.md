# Git y GitHub

El código fuente y la documentación del proyecto se versionan con **Git** y se publican en **GitHub**.
Cada avance (APF1, APF2, APF3) queda registrado como commits y una etiqueta (tag), y cada push ejecuta las pruebas en GitHub Actions.

## 1. Preparar (una sola vez)

1. Instalar Git: <https://git-scm.com/download/win>.
2. Configurar el autor de los commits con **tu** nombre y el correo de **tu** cuenta de GitHub:
   ```bash
   git config --global user.name "Luis Osmin Galecio Jaramillo"
   git config --global user.email "tu-correo@ejemplo.com"
   ```
3. Crear en <https://github.com/new> un repositorio **vacío** llamado `cruzma-ventas` (sin README ni .gitignore).

## 2. Primera subida

Desde la carpeta del proyecto (`C:\proyectos\cruzma-ventas`):

```bat
scripts\subir-a-github.bat https://github.com/TU_USUARIO/cruzma-ventas.git
```

El script crea el repositorio local, hace un commit por capa (base de datos, modelo y DAO, servicios, web, pruebas, documentación),
crea la etiqueta `v0.3.0` y lo sube. La primera vez Git abrirá el navegador para iniciar sesión en GitHub.

También se puede hacer desde NetBeans: *Team → Git → Initialize Repository*, luego *Commit* y *Remote → Push*.

## 3. Trabajo diario

| Paso | Comando |
|------|---------|
| Ver qué cambió | `git status` |
| Crear una rama para un cambio | `git switch -c feature/reporte-pdf` |
| Guardar el avance | `git add .` y `git commit -m "feat(reportes): exportar a PDF"` |
| Subir | `git push -u origin feature/reporte-pdf` |
| Integrar | Abrir un *Pull Request* en GitHub hacia `main`; se integra cuando las pruebas pasan |
| Marcar un entregable | `git tag -a v0.4.0 -m "Proyecto final"` y `git push origin v0.4.0` |

## 4. Convenciones

- **Ramas**: `main` (versión entregable), `develop` (integración), `feature/<tema>` (cada cambio).
- **Mensajes** (Conventional Commits): `feat:` funcionalidad, `fix:` corrección, `test:` pruebas, `docs:` documentación,
  `refactor:` mejora sin cambiar comportamiento, `chore:` configuración.
- **Etiquetas por entregable**: `v0.1.0` y `v0.2.0` (APF2), `v0.3.0` (APF3). El detalle está en `CHANGELOG.md`.
- **Lo que no se sube** (`.gitignore`): `target/`, `logs/`, archivos del IDE y `.env` con credenciales.
  Las claves del OSE y de la base de datos van en variables de entorno, nunca en el repositorio.

## 5. Evidencia para el informe

- Captura de la página del repositorio (archivos y README).
- Captura de *Insights → Commits* o del historial de commits.
- Captura de la pestaña *Actions* con el check verde de "Compilar y probar".
- Captura de *Releases/Tags* con `v0.3.0`.
- Enlace al repositorio en la carátula del informe.
