package pe.cruzma.ventas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Persona o empresa que solicita cotizaciones y compra a Grupo Cruzma (RF03).
 * <p>
 * Tabla {@code cliente}. Las anotaciones de validación revisan el formulario;
 * {@link #documentoValido()} aplica además la regla de SUNAT según el tipo de
 * documento.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @NotNull(message = "Elige DNI o RUC")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_doc", nullable = false, length = 3)
    private TipoDocumento tipoDoc;

    @NotBlank(message = "Ingresa el número de documento")
    @Pattern(regexp = "\\d{8}|\\d{11}", message = "El DNI tiene 8 dígitos y el RUC 11")
    @Column(name = "num_doc", nullable = false, unique = true, length = 11)
    private String numDoc;

    @NotBlank(message = "Ingresa el nombre o la razón social")
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(length = 200)
    private String direccion;

    @Column(length = 15)
    private String telefono;

    @Email(message = "El correo no es válido")
    @Column(length = 100)
    private String email;

    /**
     * Indica si el número de documento es válido para su tipo.
     * <p>
     * DNI: 8 dígitos. RUC: 11 dígitos que empiezan en 10, 15, 17 o 20.
     *
     * @return {@code true} si el documento cumple la regla de su tipo
     */
    public boolean documentoValido() {
        if (tipoDoc == null || numDoc == null) return false;
        return tipoDoc == TipoDocumento.DNI ? numDoc.matches("\\d{8}") : numDoc.matches("(10|15|17|20)\\d{9}");
    }

    /** Crea un cliente vacío; lo usan JPA y el formulario de clientes. */
    public Cliente() { }

    /** {@return identificador} */
    public Long getId() { return id; }
    /**
     * Asigna identificador.
     *
     * @param id nuevo valor
     */
    public void setId(Long id) { this.id = id; }
    /** {@return tipo de documento} */
    public TipoDocumento getTipoDoc() { return tipoDoc; }
    /**
     * Asigna tipo de documento.
     *
     * @param tipoDoc nuevo valor
     */
    public void setTipoDoc(TipoDocumento tipoDoc) { this.tipoDoc = tipoDoc; }
    /** {@return número de DNI o RUC} */
    public String getNumDoc() { return numDoc; }
    /**
     * Asigna número de DNI o RUC.
     *
     * @param numDoc nuevo valor
     */
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }
    /** {@return nombre o razón social} */
    public String getRazonSocial() { return razonSocial; }
    /**
     * Asigna nombre o razón social.
     *
     * @param razonSocial nuevo valor
     */
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    /** {@return dirección} */
    public String getDireccion() { return direccion; }
    /**
     * Asigna dirección.
     *
     * @param direccion nuevo valor
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }
    /** {@return teléfono} */
    public String getTelefono() { return telefono; }
    /**
     * Asigna teléfono.
     *
     * @param telefono nuevo valor
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }
    /** {@return correo electrónico} */
    public String getEmail() { return email; }
    /**
     * Asigna correo electrónico.
     *
     * @param email nuevo valor
     */
    public void setEmail(String email) { this.email = email; }
}
