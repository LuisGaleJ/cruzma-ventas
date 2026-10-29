/**
 * Entidades JPA del negocio y sus enumeraciones; mapean las tablas de {@code db/schema.sql}.
 * <p>
 * Las entidades contienen las reglas que dependen solo de sus datos: cálculo de totales,
 * saldo, separación del IGV y validación de DNI o RUC.
 * <p>
 * Las entidades con {@code @Audited} guardan su historial de cambios con Hibernate Envers;
 * {@link pe.cruzma.ventas.model.Revision} registra quién y cuándo.
 */
package pe.cruzma.ventas.model;
