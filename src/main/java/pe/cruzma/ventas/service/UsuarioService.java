package pe.cruzma.ventas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.Rol;
import pe.cruzma.ventas.model.Usuario;
import pe.cruzma.ventas.repository.RolRepository;
import pe.cruzma.ventas.repository.UsuarioRepository;
import pe.cruzma.ventas.util.Auditoria;

import java.util.List;

/**
 * Gestión de usuarios y roles (RF02).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param usuarios repositorio de usuarios
     * @param roles    repositorio de roles
     * @param encoder  codificador BCrypt de contraseñas
     */
    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    /** {@return todos los usuarios} */
    public List<Usuario> listar() { return usuarios.findAll(); }

    /** {@return los roles disponibles} */
    public List<Rol> roles() { return roles.findAll(); }

    /**
     * Obtiene un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return el usuario
     * @throws IllegalArgumentException si no existe
     */
    public Usuario porUsername(String username) {
        return usuarios.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    /**
     * Crea un usuario con la contraseña cifrada en BCrypt (RF02, RNF01).
     *
     * @param username nombre de usuario: de 4 a 30 minúsculas, números, punto o guion bajo
     * @param nombres  nombre completo
     * @param password contraseña en texto plano, de al menos 8 caracteres; no se guarda
     * @param idRol    id del rol asignado
     * @return el usuario creado
     * @throws IllegalArgumentException si algún dato no cumple las reglas
     */
    @Transactional
    public Usuario crear(String username, String nombres, String password, Long idRol) {
        if (username == null || !username.matches("[a-z0-9._]{4,30}")) {
            throw new IllegalArgumentException("El usuario debe tener de 4 a 30 caracteres: minúsculas, números, punto o guion bajo");
        }
        if (usuarios.existsByUsername(username)) throw new IllegalArgumentException("El usuario " + username + " ya existe");
        if (nombres == null || nombres.isBlank()) throw new IllegalArgumentException("Ingresa los nombres");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombres(nombres.trim());
        u.setPasswordHash(encoder.encode(password));
        u.setRol(roles.findById(idRol).orElseThrow(() -> new IllegalArgumentException("Rol no válido")));
        Usuario guardado = usuarios.save(u);
        Auditoria.registrar("CREAR_USUARIO", username + " rol=" + u.getRol().getNombre());
        return guardado;
    }

    /**
     * Actualiza los nombres, el rol y, si se indica, la contraseña de un usuario.
     * El nombre de usuario no cambia, porque identifica al autor en la auditoría.
     *
     * @param id             id del usuario a editar
     * @param nombres        nombre completo
     * @param idRol          id del rol asignado
     * @param nuevaPassword  contraseña nueva de al menos 8 caracteres, o vacía para conservar la actual
     * @param usernameActual usuario que hace el cambio; no puede cambiar su propio rol
     * @return el usuario actualizado
     * @throws IllegalArgumentException si algún dato no cumple las reglas
     * @since 0.4.1
     */
    @Transactional
    public Usuario actualizar(Long id, String nombres, Long idRol, String nuevaPassword, String usernameActual) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (nombres == null || nombres.isBlank()) throw new IllegalArgumentException("Ingresa los nombres");
        Rol rol = roles.findById(idRol).orElseThrow(() -> new IllegalArgumentException("Rol no válido"));
        if (u.getUsername().equals(usernameActual) && !rol.getNombre().equals(u.getRol().getNombre())) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol");
        }
        boolean cambiaClave = nuevaPassword != null && !nuevaPassword.isEmpty();
        if (cambiaClave && nuevaPassword.length() < 8) throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        u.setNombres(nombres.trim());
        u.setRol(rol);
        if (cambiaClave) u.setPasswordHash(encoder.encode(nuevaPassword));
        Auditoria.registrar(usernameActual, "EDITAR_USUARIO",
                u.getUsername() + " rol=" + rol.getNombre() + (cambiaClave ? " contraseña=cambiada" : ""));
        return usuarios.save(u);
    }

    /**
     * Activa o desactiva un usuario.
     *
     * @param id             id del usuario a cambiar
     * @param usernameActual usuario que hace el cambio; no puede desactivarse a sí mismo
     * @throws IllegalArgumentException si el usuario no existe o es el propio usuario
     */
    @Transactional
    public void cambiarEstado(Long id, String usernameActual) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (u.getUsername().equals(usernameActual)) throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
        u.setActivo(!u.isActivo());
        Auditoria.registrar(usernameActual, "CAMBIAR_ESTADO_USUARIO", u.getUsername() + " activo=" + u.isActivo());
    }
}
