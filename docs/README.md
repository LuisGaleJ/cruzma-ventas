# Documentación técnica – Cruzma Ventas

Documentación para desarrolladores del sistema de cotizaciones, ventas y facturación electrónica de Grupo Cruzma.
Complementa el [README del proyecto](../README.md), que explica cómo instalarlo y ejecutarlo.

| Documento | Contenido |
|-----------|-----------|
| [Arquitectura](arquitectura.md) | Capas, paquetes, flujo de una petición y dependencias |
| [Modelo de datos](modelo-de-datos.md) | Entidades, tablas, relaciones y restricciones |
| [Reglas de negocio](reglas-de-negocio.md) | Reglas de SUNAT y de la empresa, y dónde está cada una en el código |
| [Rutas y pantallas](rutas-y-pantallas.md) | Cada URL, su controlador, método, roles y requisito |
| [Guía del desarrollador](guia-del-desarrollador.md) | Convenciones, cómo documentar, probar, agregar un módulo y conectar el OSE real |
| [Cumplimiento del APF3](apf3-cumplimiento.md) | Principios MVC, DAO, SOLID y TDD, seguridad, librerías, Git y alcance |
| [Git y GitHub](git-y-github.md) | Cómo subir el proyecto y registrar los avances |
| [Javadoc](javadoc/index.html) | Referencia generada de todas las clases y métodos |

## Cómo se documenta el código

- **Javadoc** en todas las clases, constructores, métodos públicos y constantes, con `@param`, `@return`, `@throws`,
  `@author` y `@since`. Cada paquete tiene su `package-info.java`.
- Los comentarios dicen **qué hace y por qué** (la regla de negocio o el requisito RF/RNF que cumple), no repiten el código.
- La documentación se revisa con `doclint`: `mvn javadoc:javadoc` no debe mostrar advertencias.
- Para regenerar la referencia HTML: `mvn javadoc:javadoc` → `target/site/apidocs/index.html`.
  La carpeta `docs/javadoc/` contiene una copia ya generada.
