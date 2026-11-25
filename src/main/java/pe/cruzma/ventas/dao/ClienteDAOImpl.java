package pe.cruzma.ventas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.cruzma.ventas.dto.ClienteDTO;

/**
 * Implementación JDBC del {@link ClienteDAO} sobre la tabla {@code cliente}.
 * <p>
 * Todas las sentencias usan {@link PreparedStatement} con parámetros (sin concatenar
 * texto), lo que evita la inyección SQL. Los métodos devuelven DTO o listas de DTO,
 * nunca un {@link ResultSet}, para que la capa de negocio no dependa de JDBC.
 *
 * @author Grupo Y - ADSI
 */
@Repository("clienteDAO")
public class ClienteDAOImpl implements ClienteDAO {

    private static final String COLUMNAS = "id_cliente, tipo_doc, num_doc, razon_social, direccion, telefono, email";
    private static final String SQL_INSERT =
            "INSERT INTO cliente (tipo_doc, num_doc, razon_social, direccion, telefono, email) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ID = "SELECT " + COLUMNAS + " FROM cliente WHERE id_cliente = ?";
    private static final String SQL_UPDATE =
            "UPDATE cliente SET tipo_doc = ?, num_doc = ?, razon_social = ?, direccion = ?, telefono = ?, email = ? WHERE id_cliente = ?";
    private static final String SQL_DELETE = "DELETE FROM cliente WHERE id_cliente = ?";
    private static final String SQL_SELECT_ALL = "SELECT " + COLUMNAS + " FROM cliente ORDER BY razon_social";
    private static final String SQL_SELECT_DOC = "SELECT " + COLUMNAS + " FROM cliente WHERE num_doc = ?";
    private static final String SQL_COUNT_TIPO = "SELECT COUNT(*) FROM cliente WHERE tipo_doc = ?";

    private final ConexionBD conexion;

    /**
     * Crea el DAO con la utilidad de conexión.
     *
     * @param conexion fuente de conexiones (pool HikariCP)
     */
    public ClienteDAOImpl(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public Long create(ClienteDTO dto) {
        try (Connection cn = conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, dto);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    dto.setId(rs.getLong(1));
                }
            }
            return dto.getId();
        } catch (SQLException e) {
            throw new DataAccessDAOException("No se pudo registrar el cliente " + dto.getNumDoc(), e);
        }
    }

    @Override
    public Optional<ClienteDTO> read(Long id) {
        return consultarUno(SQL_SELECT_ID, ps -> ps.setLong(1, id));
    }

    @Override
    public boolean update(ClienteDTO dto) {
        try (Connection cn = conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_UPDATE)) {
            asignarParametros(ps, dto);
            ps.setLong(7, dto.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessDAOException("No se pudo actualizar el cliente " + dto.getId(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Connection cn = conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessDAOException("No se pudo eliminar el cliente " + id, e);
        }
    }

    @Override
    public List<ClienteDTO> findAll() {
        List<ClienteDTO> lista = new ArrayList<>();
        try (Connection cn = conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DataAccessDAOException("No se pudo listar los clientes", e);
        }
    }

    @Override
    public Optional<ClienteDTO> buscarPorDocumento(String numDoc) {
        return consultarUno(SQL_SELECT_DOC, ps -> ps.setString(1, numDoc));
    }

    @Override
    public int contarPorTipoDocumento(String tipoDoc) {
        try (Connection cn = conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_COUNT_TIPO)) {
            ps.setString(1, tipoDoc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessDAOException("No se pudo contar los clientes " + tipoDoc, e);
        }
    }

    // ------------------------------------------------------------------ apoyo

    /** Parámetros que se asignan a una sentencia antes de ejecutarla. */
    @FunctionalInterface
    private interface Parametros {
        void asignar(PreparedStatement ps) throws SQLException;
    }

    private Optional<ClienteDTO> consultarUno(String sql, Parametros parametros) {
        try (Connection cn = conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessDAOException("No se pudo consultar el cliente", e);
        }
    }

    private static void asignarParametros(PreparedStatement ps, ClienteDTO dto) throws SQLException {
        ps.setString(1, dto.getTipoDoc());
        ps.setString(2, dto.getNumDoc());
        ps.setString(3, dto.getRazonSocial());
        ps.setString(4, dto.getDireccion());
        ps.setString(5, dto.getTelefono());
        ps.setString(6, dto.getEmail());
    }

    private static ClienteDTO mapear(ResultSet rs) throws SQLException {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(rs.getLong("id_cliente"));
        dto.setTipoDoc(rs.getString("tipo_doc"));
        dto.setNumDoc(rs.getString("num_doc"));
        dto.setRazonSocial(rs.getString("razon_social"));
        dto.setDireccion(rs.getString("direccion"));
        dto.setTelefono(rs.getString("telefono"));
        dto.setEmail(rs.getString("email"));
        return dto;
    }
}
