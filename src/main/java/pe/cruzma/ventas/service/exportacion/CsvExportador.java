package pe.cruzma.ventas.service.exportacion;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.util.DatosSeguros;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Exporta las ventas del período en CSV estándar (RFC 4180), para otros sistemas (RF14).
 * <p>
 * Cada valor se escapa con Apache Commons Text y se neutraliza contra inyección de fórmulas.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
@Component
public class CsvExportador implements ExportadorReporte {

    /** Crea el exportador; lo instancia Spring. */
    public CsvExportador() { }

    @Override
    public String formato() {
        return "csv";
    }

    @Override
    public String tipoContenido() {
        return "text/csv; charset=UTF-8";
    }

    @Override
    public byte[] exportar(DatosReporte datos) {
        StringBuilder sb = new StringBuilder("﻿"); // BOM: Excel reconoce las tildes
        sb.append("Fecha,Venta,Cliente,Documento,Total,Pagado,Saldo,Estado\r\n");
        for (Venta v : datos.ventas()) {
            StringJoiner fila = new StringJoiner(",");
            fila.add(v.getFecha().toLocalDate().toString())
                .add(String.valueOf(v.getId()))
                .add(campo(v.getCliente().getRazonSocial()))
                .add(campo(v.getCliente().getNumDoc()))
                .add(v.getTotal().toPlainString())
                .add(v.getPagado().toPlainString())
                .add(v.getSaldo().toPlainString())
                .add(v.getEstado().name());
            sb.append(fila).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String campo(String valor) {
        return StringEscapeUtils.escapeCsv(DatosSeguros.celdaSegura(valor));
    }
}
