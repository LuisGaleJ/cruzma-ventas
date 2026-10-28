package pe.cruzma.ventas.service.exportacion;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.dto.FilaReporte;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.util.DatosSeguros;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Exporta los reportes a un libro de Excel (.xlsx) con Apache POI (RF14).
 * <p>
 * Genera seis hojas: Resumen, Ventas, Por día (R1), Clientes y Productos (R2) y
 * Por cobrar (R3). Los montos se guardan como números para que el gerente pueda sumarlos
 * y los textos se guardan como celdas de texto; si parecen una fórmula se marcan con
 * "prefijo de comilla" para que Excel nunca los ejecute (prevención de inyección de fórmulas).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
@Component
public class ExcelExportador implements ExportadorReporte {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelExportador.class);

    /** Crea el exportador; lo instancia Spring. */
    public ExcelExportador() { }

    @Override
    public String formato() {
        return "xlsx";
    }

    @Override
    public String tipoContenido() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public byte[] exportar(DatosReporte datos) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);
            hojaResumen(libro, e, datos);
            hojaVentas(libro, e, datos.ventas());
            hojaFilas(libro, e, "Por día (R1)", "Día", "Ventas", datos.porDia());
            hojaFilas(libro, e, "Clientes (R2)", "Cliente", "Ventas", datos.porCliente());
            hojaFilas(libro, e, "Productos (R2)", "Producto", "Unidades", datos.porProducto());
            hojaPorCobrar(libro, e, datos.porCobrar());
            libro.write(salida);
            LOG.debug("Excel generado: {} ventas del {} al {}", datos.numeroVentas(), datos.desde(), datos.hasta());
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el Excel", ex);
        }
    }

    private void hojaResumen(Workbook libro, Estilos e, DatosReporte d) {
        Sheet hoja = libro.createSheet("Resumen");
        texto(e, hoja.createRow(0), 0, "Reporte de ventas - Grupo Cruzma", e.titulo);
        texto(e, hoja.createRow(1), 0, "Del " + d.desde() + " al " + d.hasta(), null);
        Row r = hoja.createRow(3);
        texto(e, r, 0, "Total vendido (S/)", e.encabezado);
        dinero(r, 1, d.total(), e.dinero);
        r = hoja.createRow(4);
        texto(e, r, 0, "Número de ventas", e.encabezado);
        r.createCell(1).setCellValue(d.numeroVentas());
        r = hoja.createRow(5);
        texto(e, r, 0, "Ticket promedio (S/)", e.encabezado);
        dinero(r, 1, d.ticketPromedio(), e.dinero);
        anchos(hoja, 26, 16);
    }

    private void hojaVentas(Workbook libro, Estilos e, List<Venta> ventas) {
        Sheet hoja = libro.createSheet("Ventas");
        encabezados(hoja, e, "Fecha", "Venta", "Cliente", "Documento", "Total", "Pagado", "Saldo", "Estado");
        int i = 1;
        for (Venta v : ventas) {
            Row r = hoja.createRow(i++);
            texto(e, r, 0, v.getFecha().toLocalDate().toString(), null);
            r.createCell(1).setCellValue(v.getId() == null ? 0 : v.getId());
            texto(e, r, 2, v.getCliente().getRazonSocial(), null);
            texto(e, r, 3, v.getCliente().getNumDoc(), null);
            dinero(r, 4, v.getTotal(), e.dinero);
            dinero(r, 5, v.getPagado(), e.dinero);
            dinero(r, 6, v.getSaldo(), e.dinero);
            texto(e, r, 7, v.getEstado().name(), null);
        }
        anchos(hoja, 12, 8, 40, 14, 12, 12, 12, 12);
    }

    private void hojaFilas(Workbook libro, Estilos e, String nombre, String etiqueta, String cantidad, List<FilaReporte> filas) {
        Sheet hoja = libro.createSheet(nombre);
        encabezados(hoja, e, etiqueta, cantidad, "Monto (S/)");
        int i = 1;
        for (FilaReporte f : filas) {
            Row r = hoja.createRow(i++);
            texto(e, r, 0, f.etiqueta(), null);
            r.createCell(1).setCellValue(f.cantidad());
            dinero(r, 2, f.monto(), e.dinero);
        }
        anchos(hoja, 44, 12, 14);
    }

    private void hojaPorCobrar(Workbook libro, Estilos e, List<Venta> ventas) {
        Sheet hoja = libro.createSheet("Por cobrar (R3)");
        encabezados(hoja, e, "Venta", "Cliente", "Fecha", "Días", "Saldo (S/)");
        int i = 1;
        for (Venta v : ventas) {
            Row r = hoja.createRow(i++);
            r.createCell(0).setCellValue(v.getId() == null ? 0 : v.getId());
            texto(e, r, 1, v.getCliente().getRazonSocial(), null);
            texto(e, r, 2, v.getFecha().toLocalDate().toString(), null);
            r.createCell(3).setCellValue(v.getDiasDesdeVenta());
            dinero(r, 4, v.getSaldo(), e.dinero);
        }
        anchos(hoja, 8, 40, 12, 8, 14);
    }

    private static void encabezados(Sheet hoja, Estilos e, String... titulos) {
        Row r = hoja.createRow(0);
        for (int c = 0; c < titulos.length; c++) texto(e, r, c, titulos[c], e.encabezado);
        hoja.createFreezePane(0, 1);
    }

    private static void texto(Estilos e, Row r, int col, String valor, CellStyle estilo) {
        Cell celda = r.createCell(col);
        celda.setCellValue(valor == null ? "" : valor);
        if (DatosSeguros.pareceFormula(valor)) {
            celda.setCellStyle(e.textoProtegido);
        } else if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }

    private static void dinero(Row r, int col, BigDecimal valor, CellStyle estilo) {
        Cell celda = r.createCell(col);
        celda.setCellValue(valor == null ? 0d : valor.doubleValue());
        celda.setCellStyle(estilo);
    }

    /** Fija anchos en caracteres; evita autoSizeColumn, que necesita fuentes del sistema en el servidor. */
    private static void anchos(Sheet hoja, int... caracteres) {
        for (int c = 0; c < caracteres.length; c++) hoja.setColumnWidth(c, caracteres[c] * 256);
    }

    /** Estilos compartidos del libro; POI limita cuántos estilos se pueden crear, por eso se reutilizan. */
    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle encabezado;
        final CellStyle dinero;
        final CellStyle textoProtegido;

        Estilos(Workbook libro) {
            Font negrita = libro.createFont();
            negrita.setBold(true);
            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);

            titulo = libro.createCellStyle();
            titulo.setFont(grande);

            encabezado = libro.createCellStyle();
            encabezado.setFont(negrita);
            encabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setBorderBottom(BorderStyle.THIN);

            dinero = libro.createCellStyle();
            dinero.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));

            textoProtegido = libro.createCellStyle();
            textoProtegido.setQuotePrefixed(true);
        }
    }
}
