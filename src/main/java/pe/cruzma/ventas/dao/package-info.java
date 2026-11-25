/**
 * Patrón DAO (Data Access Object) implementado con JDBC.
 * <p>
 * A diferencia del paquete {@code repository} (patrón Repository de Spring Data JPA),
 * aquí cada operación CRUD se escribe con SQL explícito y {@link java.sql.PreparedStatement},
 * sobre la conexión que entrega {@link pe.cruzma.ventas.dao.ConexionBD} (pool HikariCP).
 * Estructura: interfaz genérica {@link pe.cruzma.ventas.dao.DataAccessObject},
 * interfaz específica {@link pe.cruzma.ventas.dao.ClienteDAO}, clase concreta
 * {@link pe.cruzma.ventas.dao.ClienteDAOImpl}, objeto de transferencia
 * {@link pe.cruzma.ventas.dto.ClienteDTO} y clase cliente
 * {@link pe.cruzma.ventas.service.CarteraClientesService}.
 */
package pe.cruzma.ventas.dao;
