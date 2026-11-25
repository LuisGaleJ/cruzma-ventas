package pe.cruzma.ventas.dao;

import java.util.Optional;

/**
 * Interfaz DAO genérica con las cuatro operaciones CRUD.
 * <p>
 * Se usa una sola interfaz genérica para no repetir el CRUD en cada DAO;
 * las interfaces específicas la extienden con las consultas propias del negocio.
 *
 * @param <T> tipo del objeto de transferencia (DTO)
 * @param <K> tipo de la clave primaria
 * @author Grupo Y - ADSI
 */
public interface DataAccessObject<T, K> {

    /**
     * Inserta un registro nuevo.
     *
     * @param dto datos a guardar
     * @return clave primaria generada
     */
    K create(T dto);

    /**
     * Lee un registro por su clave.
     *
     * @param id clave primaria
     * @return el registro, o vacío si no existe
     */
    Optional<T> read(K id);

    /**
     * Actualiza un registro existente.
     *
     * @param dto datos con la clave del registro
     * @return {@code true} si se actualizó una fila
     */
    boolean update(T dto);

    /**
     * Elimina un registro.
     *
     * @param id clave primaria
     * @return {@code true} si se eliminó una fila
     */
    boolean delete(K id);
}
