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
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.dto.DetalleReporte;
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
 * Genera siete hojas: Resumen, Ventas, Detalle por producto, Por día (R1), Clientes y Productos (R2) y
 * Por cobrar (R3). Las ventas, clientes y productos muestran el monto original (sin descuentos), el descuento
 * concedido y el total después del descuento; las hojas de ventas y detalle terminan con una fila de totales. Los montos se guardan como números para que el gerente pueda sumarlos
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
            hojaDetalle(libro, e, datos.detalle());
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
        r = hoja.createRow(6);
        texto(e, r, 0, "Monto original (S/)", e.encabezado);
        dinero(r, 1, d.totalOriginal(), e.dinero);
        r = hoja.createRow(7);
        texto(e, r, 0, "Descuentos concedidos (S/)", e.encabezado);
        dinero(r, 1, d.totalDescuento(), e.dinero);
        anchos(hoja, 28, 16);
    }

    private void hojaVentas(Workbook libro, Estilos e, List<Venta> ventas) {
        Sheet hoja = libro.createSheet("Ventas");
        encabezados(hoja, e, "Fecha", "Venta", "Cotización", "Cliente", "Documento", "Monto original (S/)",
                "Descuento (S/)", "Total con descuento (S/)", "Pagado (S/)", "Saldo (S/)", "Estado");
        int i = 1;
        for (Venta v : ventas) {
            Row r = hoja.createRow(i++);
            texto(e, r, 0, v.getFecha().toLocalDate().toString(), null);
            r.createCell(1).setCellValue(v.getId() == null ? 0 : v.getId());
            texto(e, r, 2, v.getCotizacion() == null ? "" : v.getCotizacion().getNumero(), null);
            texto(e, r, 3, v.getCliente().getRazonSocial(), null);
            texto(e, r, 4, v.getCliente().getNumDoc(), null);
            dinero(r, 5, v.getMontoOriginal(), e.dinero);
            dinero(r, 6, v.getMontoDescuento(), e.dinero);
            dinero(r, 7, v.getTotal(), e.dinero);
            dinero(r, 8, v.getPagado(), e.dinero);
            dinero(r, 9, v.getSaldo(), e.dinero);
            texto(e, r, 10, v.getEstado().name(), null);
        }
        filaTotales(hoja, e, i, 3, 5, 6, 7, 8, 9);
        anchos(hoja, 12, 8, 15, 40, 14, 18, 15, 22, 13, 13, 12);
    }

    /** Un ítem vendido por fila: monto original, descuento (del ítem más su parte del global) y total con descuento. */
    private void hojaDetalle(Workbook libro, Estilos e, List<DetalleReporte> detalle) {
        Sheet hoja = libro.createSheet("Detalle por producto");
        encabezados(hoja, e, "Fecha", "Venta", "Cotización", "Cliente", "Producto o servicio", "Unidad", "Cant.",
                "Medidas", "P. unit. (S/)", "Monto original (S/)", "Desc. ítem %", "Desc. global %",
                "Descuento (S/)", "Total con descuento (S/)");
        int i = 1;
        for (DetalleReporte d : detalle) {
            Row r = hoja.createRow(i++);
            texto(e, r, 0, d.fecha().toString(), null);
            r.createCell(1).setCellValue(d.venta() == null ? 0 : d.venta());
            texto(e, r, 2, d.cotizacion(), null);
            texto(e, r, 3, d.cliente(), null);
            texto(e, r, 4, d.producto(), null);
            texto(e, r, 5, d.unidad(), null);
            r.createCell(6).setCellValue(d.cantidad());
            texto(e, r, 7, d.medidas(), null);
            dinero(r, 8, d.precio(), e.dinero);
            dinero(r, 9, d.montoOriginal(), e.dinero);
            dinero(r, 10, d.descuentoItem(), e.dinero);
            dinero(r, 11, d.descuentoGlobal(), e.dinero);
            dinero(r, 12, d.montoDescuento(), e.dinero);
            dinero(r, 13, d.montoCobrado(), e.dinero);
        }
        filaTotales(hoja, e, i, 4, 9, 12, 13);
        anchos(hoja, 12, 8, 15, 34, 38, 8, 7, 14, 13, 18, 12, 13, 15, 22);
    }

    private void hojaFilas(Workbook libro, Estilos e, String nombre, String etiqueta, String cantidad, List<FilaReporte> filas) {
        Sheet hoja = libro.createSheet(nombre);
        encabezados(hoja, e, etiqueta, cantidad, "Monto original (S/)", "Descuento (S/)", "Total con descuento (S/)");
        int i = 1;
        for (FilaReporte f : filas) {
            Row r = hoja.createRow(i++);
            texto(e, r, 0, f.etiqueta(), null);
            r.createCell(1).setCellValue(f.cantidad());
            dinero(r, 2, f.montoOriginal(), e.dinero);
            dinero(r, 3, f.descuento(), e.dinero);
            dinero(r, 4, f.monto(), e.dinero);
        }
        filaTotales(hoja, e, i, 0, 2, 3, 4);
        anchos(hoja, 44, 12, 18, 15, 22);
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

    /**
     * Fila «TOTAL» al final de la hoja con sumas (fórmulas SUM, que el sistema genera, no texto del usuario).
     *
     * @param fila          número de la fila de totales (la siguiente a la última con datos)
     * @param colEtiqueta   columna donde se escribe «TOTAL»
     * @param columnas      columnas de dinero que se suman
     */
    private static void filaTotales(Sheet hoja, Estilos e, int fila, int colEtiqueta, int... columnas) {
        if (fila <= 1) return;                                   // sin datos, no hay totales
        Row r = hoja.createRow(fila);
        texto(e, r, colEtiqueta, "TOTAL", e.encabezado);
        for (int c : columnas) {
            String letra = CellReference.convertNumToColString(c);
            Cell celda = r.createCell(c);
            celda.setCellFormula("SUM(" + letra + "2:" + letra + fila + ")");
            celda.setCellStyle(e.dineroTotal);
        }
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
        final CellStyle dineroTotal;
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

            dineroTotal = libro.createCellStyle();
            dineroTotal.cloneStyleFrom(dinero);
            dineroTotal.setFont(negrita);
            dineroTotal.setBorderTop(BorderStyle.THIN);

            textoProtegido = libro.createCellStyle();
            textoProtegido.setQuotePrefixed(true);
        }
    }
}
