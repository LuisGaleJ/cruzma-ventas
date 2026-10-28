package pe.cruzma.ventas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

/**
 * Seguridad del sistema (RNF01, RNF02):
 * contraseñas con BCrypt y acceso a cada módulo según el rol.
 */
@Configuration
public class SecurityConfig {

    /** Constructor usado por Spring. */
    public SecurityConfig() { }

    /**
     * Codificador de contraseñas.
     *
     * @return codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define qué rol puede entrar a cada ruta, la página de login y el cierre de sesión.
     *
     * @param http configuración de seguridad HTTP
     * @return la cadena de filtros de seguridad
     * @throws Exception si la configuración no es válida
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**").permitAll()
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/productos/guardar").hasRole("ADMIN")
                .requestMatchers("/reportes/**").hasAnyRole("ADMIN", "CAJA", "VENDEDOR")
                .requestMatchers("/cotizaciones/**").hasAnyRole("ADMIN", "VENDEDOR")
                .requestMatchers("/ventas/*/pagos", "/ventas/*/emitir", "/comprobantes/*/anular").hasAnyRole("ADMIN", "CAJA")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?salir").deleteCookies("JSESSIONID"))
            // Cabeceras de seguridad (APF3): no mostrar el sitio en iframes ni filtrar la URL a otros sitios
            .headers(h -> h
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicy.SAME_ORIGIN)))
            // Una sola sesión por usuario: si ingresa desde otro equipo, la sesión anterior se cierra
            .sessionManagement(s -> s.maximumSessions(1));
        return http.build();
    }
}
