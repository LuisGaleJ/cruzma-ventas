package pe.cruzma.ventas.dto;

/**
 * Objeto de transferencia de datos (DTO) de un cliente.
 * <p>
 * Solo transporta datos entre el {@link pe.cruzma.ventas.dao.ClienteDAO} y la capa de
 * negocio: no tiene anotaciones JPA ni lógica, y cada campo corresponde a una columna
 * de la tabla {@code cliente}.
 *
 * @author Grupo Y - ADSI
 */
public class ClienteDTO {

    private Long id;
    private String tipoDoc;
    private String numDoc;
    private String razonSocial;
    private String direccion;
    private String telefono;
    private String email;

    /** Crea un DTO vacío. */
    public ClienteDTO() { }

    /**
     * Crea un DTO con los datos obligatorios.
     *
     * @param tipoDoc     DNI o RUC
     * @param numDoc      número de documento
     * @param razonSocial nombre o razón social
     */
    public ClienteDTO(String tipoDoc, String numDoc, String razonSocial) {
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.razonSocial = razonSocial;
    }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** @param id identificador */
    public void setId(Long id) { this.id = id; }
    /** {@return tipo de documento (DNI o RUC)} */
    public String getTipoDoc() { return tipoDoc; }
    /** @param tipoDoc tipo de documento */
    public void setTipoDoc(String tipoDoc) { this.tipoDoc = tipoDoc; }
    /** {@return número de documento} */
    public String getNumDoc() { return numDoc; }
    /** @param numDoc número de documento */
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }
    /** {@return nombre o razón social} */
    public String getRazonSocial() { return razonSocial; }
    /** @param razonSocial nombre o razón social */
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    /** {@return dirección} */
    public String getDireccion() { return direccion; }
    /** @param direccion dirección */
    public void setDireccion(String direccion) { this.direccion = direccion; }
    /** {@return teléfono} */
    public String getTelefono() { return telefono; }
    /** @param telefono teléfono */
    public void setTelefono(String telefono) { this.telefono = telefono; }
    /** {@return correo electrónico} */
    public String getEmail() { return email; }
    /** @param email correo electrónico */
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "ClienteDTO{id=" + id + ", tipoDoc=" + tipoDoc + ", razonSocial=" + razonSocial + "}";
    }
}
