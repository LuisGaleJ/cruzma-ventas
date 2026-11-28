package pe.cruzma.ventas.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.cruzma.ventas.service.AlertaCotizacionService;

import java.security.Principal;

/**
 * Datos comunes a todas las vistas: la ruta actual (para marcar el menú)
 * y los datos de la empresa, que se configuran en application.properties.
 */
@ControllerAdvice
public class VistaGlobalAdvice {
    private final String empresaRuc;
    private final String empresaNombre;
    private final String empresaDireccion;
    private final AlertaCotizacionService alertas;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param empresaRuc       RUC de la empresa, propiedad {@code cruzma.empresa.ruc}
     * @param empresaNombre    nombre comercial, propiedad {@code cruzma.empresa.nombre}
     * @param empresaDireccion dirección, propiedad {@code cruzma.empresa.direccion}
     * @param alertas          alertas de cotizaciones por vencer
     */
    public VistaGlobalAdvice(@Value("${cruzma.empresa.ruc}") String empresaRuc,
                             @Value("${cruzma.empresa.nombre}") String empresaNombre,
                             @Value("${cruzma.empresa.direccion}") String empresaDireccion,
                             AlertaCotizacionService alertas) {
        this.empresaRuc = empresaRuc;
        this.empresaNombre = empresaNombre;
        this.empresaDireccion = empresaDireccion;
        this.alertas = alertas;
    }

    /**
     * Contador del menú: cotizaciones que vencen en los próximos días. Solo se consulta con sesión iniciada.
     *
     * @param usuario usuario de la sesión; {@code null} en el login
     * @return cantidad de cotizaciones por vencer
     */
    @ModelAttribute("cotizacionesPorVencer")
    public long cotizacionesPorVencer(Principal usuario) {
        return usuario == null ? 0 : alertas.contarPorVencer();
    }

    /** {@return días de anticipación del aviso, para marcar las filas por vencer} */
    @ModelAttribute("diasAviso")
    public int diasAviso() { return alertas.diasAviso(); }

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param request petición actual
     * @return ruta de la petición, para marcar el módulo activo del menú
     */
    @ModelAttribute("uri")
    public String uri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /** {@return RUC de la empresa para las vistas} */
    @ModelAttribute("empresaRuc")
    public String empresaRuc() { return empresaRuc; }

    /** {@return nombre de la empresa para las vistas} */
    @ModelAttribute("empresaNombre")
    public String empresaNombre() { return empresaNombre; }

    /** {@return dirección de la empresa para las vistas} */
    @ModelAttribute("empresaDireccion")
    public String empresaDireccion() { return empresaDireccion; }
}
