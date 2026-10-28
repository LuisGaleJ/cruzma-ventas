package pe.cruzma.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del sistema de facturación y control de ventas de Grupo Cruzma.
 * <p>
 * Arranca Spring Boot, que levanta el servidor web embebido (puerto 8080),
 * la seguridad, la conexión a la base de datos y los datos de demostración.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@SpringBootApplication
public class CruzmaVentasApplication {

    /** Constructor usado por Spring. */
    public CruzmaVentasApplication() { }

    /**
     * Inicia la aplicación.
     *
     * @param args argumentos de línea de comandos; por ejemplo
     *             {@code --spring.profiles.active=mysql} para usar MySQL
     */
    public static void main(String[] args) {
        SpringApplication.run(CruzmaVentasApplication.class, args);
    }
}
