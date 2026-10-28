@echo off
REM ==========================================================================
REM  Sube el proyecto Cruzma Ventas a GitHub por primera vez.
REM  Antes: crea un repositorio VACIO en https://github.com/new (sin README).
REM  Uso: abre "cmd" en la carpeta del proyecto y ejecuta:
REM       scripts\subir-a-github.bat https://github.com/TU_USUARIO/cruzma-ventas.git
REM ==========================================================================
if "%~1"=="" (
  echo Falta la URL del repositorio. Ejemplo:
  echo   scripts\subir-a-github.bat https://github.com/TU_USUARIO/cruzma-ventas.git
  exit /b 1
)
git --version || (echo Instala Git desde https://git-scm.com & exit /b 1)

git init -b main
git add .gitignore pom.xml nbactions.xml
git commit -m "chore: estructura del proyecto Maven y Spring Boot"
git add db
git commit -m "feat(bd): diseno fisico de la base de datos en MySQL"
git add src/main/java/pe/cruzma/ventas/model src/main/java/pe/cruzma/ventas/repository
git commit -m "feat(modelo): entidades JPA y repositorios (DAO)"
git add src/main/java/pe/cruzma/ventas/service src/main/java/pe/cruzma/ventas/integration src/main/java/pe/cruzma/ventas/dto src/main/java/pe/cruzma/ventas/util
git commit -m "feat(servicios): reglas de negocio, OSE, exportacion y seguridad de datos"
git add src/main/java src/main/resources
git commit -m "feat(web): controladores MVC, vistas Thymeleaf, seguridad y Logback"
git add src/test
git commit -m "test: pruebas unitarias con JUnit 5 y Mockito"
git add .
git commit -m "docs: README, documentacion tecnica, Javadoc, CHANGELOG y CI"
git tag -a v0.3.0 -m "Avance de Proyecto Final 3"

git remote add origin %1
git push -u origin main
git push origin v0.3.0
echo.
echo Listo. Revisa el repositorio en el navegador: %1
