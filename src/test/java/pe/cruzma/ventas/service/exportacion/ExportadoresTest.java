package pe.cruzma.ventas.service.exportacion;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.dto.FilaReporte;
import pe.cruzma.ventas.model.Cotizacion;
import pe.cruzma.ventas.model.DetalleCotizacion;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.model.TipoDocumento;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.service.Fixtures;
import pe.cruzma.ventas.service.ReporteService;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la exportación a Excel (Apache POI) y CSV (Apache Commons Text). */
class ExportadoresTest {

    /** Incluye un cliente cuyo nombre empieza con "=" para probar la inyección de fórmulas. */
    private DatosReporte datos() {
        Venta normal = Fixtures.venta(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo, Ruiz"));
        Venta maliciosa = Fixtures.venta(Fixtures.cliente(TipoDocumento.DNI, "45871237", "=HYPERLINK(\"http://x\")"));
        BigDecimal total = new BigDecimal("1920.00");
        LocalDate hoy = LocalDate.now();
        return new DatosReporte(hoy.withDayOfMonth(1), hoy, List.of(normal, maliciosa), total, new BigDecimal("960.00"),
                List.of(new FilaReporte(hoy.toString(), 2, total)), List.of(), List.of(), List.of(normal));
    }

    @Test
    void excelTieneSieteHojasYMontosNumericos() throws Exception {
        byte[] archivo = new ExcelExportador().exportar(datos());
        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            assertEquals(7, libro.getNumberOfSheets());
            assertEquals(1920.00, libro.getSheet("Resumen").getRow(3).getCell(1).getNumericCellValue(), 0.001);
            Sheet ventas = libro.getSheet("Ventas");
            assertEquals(960.00, ventas.getRow(1).getCell(7).getNumericCellValue(), 0.001);
            assertEquals("SUM(H2:H3)", ventas.getRow(3).getCell(7).getCellFormula(), "fila de totales");
            var celda = ventas.getRow(2).getCell(3);
            assertEquals("=HYPERLINK(\"http://x\")", celda.getStringCellValue(), "se guarda como texto, no como fórmula");
            assertTrue(celda.getCellStyle().getQuotePrefixed(), "Excel la muestra como texto literal");
        }
    }

    @Test
    void csvEscapaComasYNeutralizaFormulas() {
        String csv = new String(new CsvExportador().exportar(datos()), StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"María Castillo, Ruiz\""), "la coma del nombre no debe partir la columna");
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"http://x\"\")\""), "la fórmula queda como texto");
        assertEquals(3, csv.trim().split("\r\n").length, "encabezado más dos ventas");
    }

    /**
     * Ejemplo real: reja de S/ 235 por m2 con 2 m2 y 10 % de descuento, instalación de S/ 250 con 5 %
     * y 2 % de descuento global. Original S/ 720.00, descuento S/ 72.71, total S/ 647.29.
     */
    @Test
    void ventasClientesYDetalleMuestranMontoOriginalDescuentoYTotal() throws Exception {
        Producto reja = Fixtures.producto("Reja de fierro", "235.00");
        reja.setUnidad("MTK");
        Producto instalacion = Fixtures.producto("Instalación en obra", "250.00");
        instalacion.setTipo(TipoProducto.SERVICIO);
        instalacion.setUnidad("ZZ");
        Cotizacion cot = Fixtures.cotizacion(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz"), 10);
        cot.getDetalles().clear();
        cot.agregarDetalle(new DetalleCotizacion(reja, 1, "2", reja.getPrecioUnitario(), new BigDecimal("10")));
        cot.agregarDetalle(new DetalleCotizacion(instalacion, 1, null, instalacion.getPrecioUnitario(), new BigDecimal("5")));
        cot.setDescuento(new BigDecimal("2"));
        cot.calcularTotal();
        Venta venta = Venta.desdeCotizacion(cot, Fixtures.usuario("vendedor"));

        assertEquals(new BigDecimal("720.00"), venta.getMontoOriginal());
        assertEquals(new BigDecimal("72.71"), venta.getMontoDescuento());
        assertEquals(new BigDecimal("647.29"), venta.getTotal());

        ReporteService reportes = new ReporteService(null, null);
        List<Venta> lista = List.of(venta);
        var detalle = reportes.detalle(lista);
        LocalDate hoy = LocalDate.now();
        DatosReporte datos = new DatosReporte(hoy, hoy, lista, venta.getTotal(), venta.getTotal(), reportes.porDia(lista),
                reportes.porCliente(lista), reportes.porProducto(detalle), List.of(), detalle);

        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(new ExcelExportador().exportar(datos)))) {
            Sheet ventas = libro.getSheet("Ventas");
            assertEquals(720.00, ventas.getRow(1).getCell(5).getNumericCellValue(), 0.001);
            assertEquals(72.71, ventas.getRow(1).getCell(6).getNumericCellValue(), 0.001);
            assertEquals(647.29, ventas.getRow(1).getCell(7).getNumericCellValue(), 0.001);

            Sheet clientes = libro.getSheet("Clientes (R2)");
            assertEquals("María Castillo Ruiz", clientes.getRow(1).getCell(0).getStringCellValue());
            assertEquals(720.00, clientes.getRow(1).getCell(2).getNumericCellValue(), 0.001);
            assertEquals(72.71, clientes.getRow(1).getCell(3).getNumericCellValue(), 0.001);
            assertEquals(647.29, clientes.getRow(1).getCell(4).getNumericCellValue(), 0.001);

            Sheet porProducto = libro.getSheet("Detalle por producto");
            assertEquals("Reja de fierro", porProducto.getRow(1).getCell(4).getStringCellValue());
            assertEquals(470.00, porProducto.getRow(1).getCell(9).getNumericCellValue(), 0.001);   // 235 x 2 m2
            assertEquals(10.00, porProducto.getRow(1).getCell(10).getNumericCellValue(), 0.001);   // desc. ítem %
            assertEquals(2.00, porProducto.getRow(1).getCell(11).getNumericCellValue(), 0.001);    // desc. global %
            double cobrados = porProducto.getRow(1).getCell(13).getNumericCellValue()
                    + porProducto.getRow(2).getCell(13).getNumericCellValue();
            assertEquals(647.29, cobrados, 0.001, "la suma de los ítems es igual al total de la venta");
        }
    }
}
