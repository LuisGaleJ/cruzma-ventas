package pe.cruzma.ventas.model;

import jakarta.persistence.*;

/**
 * Rol de un usuario del sistema; define a qué módulos puede entrar (RNF02).
 * <p>
 * Tabla {@code rol}. Los nombres válidos son las constantes {@link #ADMIN},
 * {@link #VENDEDOR} y {@link #CAJA}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "rol")
public class Rol {
    /**
     * Gerente: acceso total, usuarios, catálogo y reportes.
     */
    public static final String ADMIN = "ADMIN";
    /**
     * Registra clientes, cotizaciones y las convierte en venta.
     */
    public static final String VENDEDOR = "VENDEDOR";
    /**
     * Registra pagos y emite o anula comprobantes.
     */
    public static final String CAJA = "CAJA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    /**
     * Constructor requerido por JPA.
     */
    protected Rol() { }

    /**
     * Crea un rol.
     *
     * @param nombre nombre del rol, por ejemplo {@link #ADMIN}
     */
    public Rol(String nombre) { this.nombre = nombre; }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return nombre} */
    public String getNombre() { return nombre; }
    /**
     * Asigna nombre.
     *
     * @param nombre nuevo valor
     */
    public void setNombre(String nombre) { this.nombre = nombre; }
}
