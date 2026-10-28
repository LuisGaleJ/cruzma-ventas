package pe.cruzma.ventas.model;

import jakarta.persistence.*;

/**
 * Persona que usa el sistema (administrador, vendedor o caja).
 * <p>
 * Tabla {@code usuario}. La contraseña se guarda solo como hash BCrypt y un
 * usuario desactivado no puede iniciar sesión. Los getters y setters son de
 * acceso simple y no se documentan uno por uno.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Contraseña cifrada con BCrypt (RNF01). Nunca se guarda en texto plano. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    /** Crea un usuario vacío; los datos se asignan con los setters. */
    public Usuario() { }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return nombre de usuario} */
    public String getUsername() { return username; }
    /**
     * Asigna nombre de usuario.
     *
     * @param username nuevo valor
     */
    public void setUsername(String username) { this.username = username; }
    /** {@return hash BCrypt de la contraseña} */
    public String getPasswordHash() { return passwordHash; }
    /**
     * Asigna hash BCrypt de la contraseña.
     *
     * @param passwordHash nuevo valor
     */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    /** {@return nombre completo} */
    public String getNombres() { return nombres; }
    /**
     * Asigna nombre completo.
     *
     * @param nombres nuevo valor
     */
    public void setNombres(String nombres) { this.nombres = nombres; }
    /** {@return si el usuario puede iniciar sesión} */
    public boolean isActivo() { return activo; }
    /**
     * Asigna si el usuario puede iniciar sesión.
     *
     * @param activo nuevo valor
     */
    public void setActivo(boolean activo) { this.activo = activo; }
    /** {@return rol asignado} */
    public Rol getRol() { return rol; }
    /**
     * Asigna rol asignado.
     *
     * @param rol nuevo valor
     */
    public void setRol(Rol rol) { this.rol = rol; }
}
