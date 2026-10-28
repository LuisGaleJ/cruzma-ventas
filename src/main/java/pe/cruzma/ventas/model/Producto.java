package pe.cruzma.ventas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Ítem del catálogo: un producto fabricado o un servicio (RF04).
 * <p>
 * Tabla {@code producto}. El precio unitario incluye IGV, que es como la
 * empresa cotiza a sus clientes.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @NotBlank(message = "Ingresa un código")
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "Ingresa la descripción")
    @Column(nullable = false, length = 200)
    private String descripcion;

    @NotBlank(message = "Ingresa la unidad (UND, M, M2...)")
    @Column(nullable = false, length = 10)
    private String unidad;

    @NotNull(message = "Elige producto o servicio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoProducto tipo;

    /** Precio de venta con IGV incluido. */
    @NotNull(message = "Ingresa el precio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /** Crea un producto vacío; lo usan JPA y el formulario del catálogo. */
    public Producto() { }

    /** {@return identificador} */
    public Long getId() { return id; }
    /**
     * Asigna identificador.
     *
     * @param id nuevo valor
     */
    public void setId(Long id) { this.id = id; }
    /** {@return código interno} */
    public String getCodigo() { return codigo; }
    /**
     * Asigna código interno.
     *
     * @param codigo nuevo valor
     */
    public void setCodigo(String codigo) { this.codigo = codigo; }
    /** {@return descripción} */
    public String getDescripcion() { return descripcion; }
    /**
     * Asigna descripción.
     *
     * @param descripcion nuevo valor
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    /** {@return unidad de medida (UND, M, M2, SERV)} */
    public String getUnidad() { return unidad; }
    /**
     * Asigna unidad de medida (UND, M, M2, SERV).
     *
     * @param unidad nuevo valor
     */
    public void setUnidad(String unidad) { this.unidad = unidad; }
    /** {@return tipo} */
    public TipoProducto getTipo() { return tipo; }
    /**
     * Asigna tipo.
     *
     * @param tipo nuevo valor
     */
    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }
    /** {@return precio unitario con IGV} */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    /**
     * Asigna precio unitario con IGV.
     *
     * @param precioUnitario nuevo valor
     */
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
