package pe.cruzma.ventas.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import pe.cruzma.ventas.util.Auditoria;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Bloqueo temporal del usuario tras varios intentos fallidos de inicio de sesión (seguridad, RNF01).
 * <p>
 * Cuenta los fallos en una caché de Google Guava que olvida cada contador pasados
 * {@code cruzma.login.minutos-bloqueo} minutos. Al llegar a {@code cruzma.login.max-intentos}
 * el usuario queda bloqueado hasta que el contador expira. Esto frena los ataques de fuerza bruta.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
@Service
public class IntentosLoginService {

    private final int maxIntentos;
    private final Cache<String, Integer> fallos;

    /**
     * Crea el servicio con la configuración de {@code application.properties}.
     *
     * @param maxIntentos    fallos permitidos antes del bloqueo
     * @param minutosBloqueo minutos que dura el bloqueo
     */
    @Autowired
    public IntentosLoginService(@Value("${cruzma.login.max-intentos:5}") int maxIntentos,
                                @Value("${cruzma.login.minutos-bloqueo:15}") int minutosBloqueo) {
        this(maxIntentos, minutosBloqueo, Ticker.systemTicker());
    }

    /**
     * Constructor para pruebas: permite controlar el paso del tiempo.
     *
     * @param maxIntentos    fallos permitidos antes del bloqueo
     * @param minutosBloqueo minutos que dura el bloqueo
     * @param reloj          reloj de Guava; en producción, el del sistema
     */
    @VisibleForTesting
    IntentosLoginService(int maxIntentos, int minutosBloqueo, Ticker reloj) {
        this.maxIntentos = maxIntentos;
        this.fallos = CacheBuilder.newBuilder()
                .expireAfterWrite(minutosBloqueo, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .ticker(reloj)
                .build();
    }

    /**
     * Suma un intento fallido.
     *
     * @param username usuario que falló la contraseña
     */
    public void registrarFallo(String username) {
        String clave = clave(username);
        int total = fallos.asMap().merge(clave, 1, Integer::sum);
        if (total >= maxIntentos) {
            Auditoria.registrar(clave, "LOGIN_BLOQUEADO", "intentos fallidos=" + total);
        }
    }

    /**
     * Reinicia el contador tras un inicio de sesión correcto.
     *
     * @param username usuario que ingresó
     */
    public void registrarExito(String username) {
        fallos.invalidate(clave(username));
        Auditoria.registrar(clave(username), "LOGIN_OK", "inicio de sesión correcto");
    }

    /**
     * Indica si el usuario está bloqueado.
     *
     * @param username usuario que intenta ingresar
     * @return {@code true} si alcanzó el máximo de intentos fallidos
     */
    public boolean estaBloqueado(String username) {
        Integer total = fallos.getIfPresent(clave(username));
        return total != null && total >= maxIntentos;
    }

    /**
     * Escucha los fallos de autenticación que publica Spring Security.
     *
     * @param evento evento de credenciales incorrectas
     */
    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        registrarFallo(evento.getAuthentication().getName());
    }

    /**
     * Escucha los inicios de sesión correctos que publica Spring Security.
     *
     * @param evento evento de autenticación correcta
     */
    @EventListener
    public void alIngresar(AuthenticationSuccessEvent evento) {
        registrarExito(evento.getAuthentication().getName());
    }

    private static String clave(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
