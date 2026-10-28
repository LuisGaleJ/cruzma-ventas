package pe.cruzma.ventas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.repository.*;

import java.math.BigDecimal;

/**
 * Carga datos de demostración la primera vez que arranca el sistema:
 * roles, un usuario por rol, clientes y el catálogo de productos de Grupo Cruzma.
 * Contraseña de todos los usuarios demo: Cruzma2026
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final PasswordEncoder encoder;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param roles     repositorio de roles
     * @param usuarios  repositorio de usuarios
     * @param clientes  repositorio de clientes
     * @param productos repositorio de productos
     * @param encoder   codificador BCrypt
     */
    public DataInitializer(RolRepository roles, UsuarioRepository usuarios, ClienteRepository clientes,
                           ProductoRepository productos, PasswordEncoder encoder) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.productos = productos;
        this.encoder = encoder;
    }

    /**
     * Carga los datos de demostración si la base está vacía.
     *
     * @param args argumentos de arranque, no se usan
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (roles.count() > 0) return;
        Rol admin = roles.save(new Rol(Rol.ADMIN));
        Rol vendedor = roles.save(new Rol(Rol.VENDEDOR));
        Rol caja = roles.save(new Rol(Rol.CAJA));

        usuario("admin", "Gerente General", admin);
        usuario("vendedor", "Vendedor de Taller", vendedor);
        usuario("caja", "Caja y Administración", caja);

        cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz", "Av. Grau 845, Piura", "969458712");
        cliente(TipoDocumento.RUC, "20601234567", "Restaurante El Chifa del Norte S.A.C.", "Jr. Tacna 320, Piura", "073305511");
        cliente(TipoDocumento.RUC, "20487654321", "Constructora Los Algarrobos S.A.", "Urb. Miraflores Mz B Lt 4, Castilla", "073348870");

        producto("PRD-001", "Baranda de acero inoxidable 304", "M", TipoProducto.PRODUCTO, "320.00");
        producto("PRD-002", "Mesa de trabajo inox para cocina", "UND", TipoProducto.PRODUCTO, "1450.00");
        producto("PRD-003", "Campana extractora industrial inox", "UND", TipoProducto.PRODUCTO, "2800.00");
        producto("PRD-004", "Puerta metálica de fierro con pintura", "UND", TipoProducto.PRODUCTO, "1200.00");
        producto("PRD-005", "Reja de fierro para ventana", "M2", TipoProducto.PRODUCTO, "180.00");
        producto("SRV-001", "Instalación en obra", "SERV", TipoProducto.SERVICIO, "250.00");
        producto("SRV-002", "Mantenimiento y pulido de acero inoxidable", "SERV", TipoProducto.SERVICIO, "150.00");
    }

    private void usuario(String username, String nombres, Rol rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombres(nombres);
        u.setPasswordHash(encoder.encode("Cruzma2026"));
        u.setRol(rol);
        usuarios.save(u);
    }

    private void cliente(TipoDocumento tipo, String doc, String nombre, String direccion, String telefono) {
        Cliente c = new Cliente();
        c.setTipoDoc(tipo);
        c.setNumDoc(doc);
        c.setRazonSocial(nombre);
        c.setDireccion(direccion);
        c.setTelefono(telefono);
        clientes.save(c);
    }

    private void producto(String codigo, String descripcion, String unidad, TipoProducto tipo, String precio) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setDescripcion(descripcion);
        p.setUnidad(unidad);
        p.setTipo(tipo);
        p.setPrecioUnitario(new BigDecimal(precio));
        productos.save(p);
    }
}
