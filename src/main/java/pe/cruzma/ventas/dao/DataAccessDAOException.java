package pe.cruzma.ventas.dao;

/**
 * Error de acceso a datos lanzado por los DAO. Envuelve la {@link java.sql.SQLException}
 * para que la capa de negocio no tenga que conocer JDBC.
 *
 * @author Grupo Y - ADSI
 */
public class DataAccessDAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción.
     *
     * @param mensaje descripción del error
     * @param causa   excepción original de JDBC
     */
    public DataAccessDAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
