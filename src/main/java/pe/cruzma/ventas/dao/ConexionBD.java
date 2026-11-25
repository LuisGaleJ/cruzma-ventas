package pe.cruzma.ventas.dao;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria que entrega conexiones a la base de datos.
 * <p>
 * Encapsula el {@link DataSource} que Spring Boot configura con HikariCP
 * (pool de conexiones) a partir de {@code spring.datasource.*}: H2 en la demostración
 * y MySQL 8 ({@code cruzma_ventas}) con el perfil {@code mysql}.
 *
 * @author Grupo Y - ADSI
 */
@Component
public class ConexionBD {

    private final DataSource dataSource;

    /**
     * Crea la utilidad con el pool configurado por Spring Boot.
     *
     * @param dataSource pool de conexiones HikariCP
     */
    public ConexionBD(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Obtiene una conexión del pool. Quien la pide debe cerrarla (try-with-resources),
     * lo que la devuelve al pool.
     *
     * @return conexión abierta
     * @throws SQLException si la base de datos no responde
     */
    public Connection obtenerConexion() throws SQLException {
        return dataSource.getConnection();
    }
}
