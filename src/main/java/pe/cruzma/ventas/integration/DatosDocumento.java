package pe.cruzma.ventas.integration;

/**
 * Datos públicos de una persona (RENIEC) o empresa (SUNAT) obtenidos por número de documento.
 *
 * @param numero    DNI o RUC consultado
 * @param nombre    nombre completo o razón social
 * @param direccion dirección fiscal (solo RUC); {@code null} si no aplica
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.5.0
 */
public record DatosDocumento(String numero, String nombre, String direccion) { }
