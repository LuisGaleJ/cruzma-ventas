package pe.cruzma.ventas.service.exportacion;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.dto.FilaReporte;
import pe.cruzma.ventas.model.TipoDocumento;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.service.Fixtures;

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
    void excelTieneSeisHojasYMontosNumericos() throws Exception {
        byte[] archivo = new ExcelExportador().exportar(datos());
        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            assertEquals(6, libro.getNumberOfSheets());
            assertEquals(1920.00, libro.getSheet("Resumen").getRow(3).getCell(1).getNumericCellValue(), 0.001);
            Sheet ventas = libro.getSheet("Ventas");
            assertEquals(960.00, ventas.getRow(1).getCell(4).getNumericCellValue(), 0.001);
            var celda = ventas.getRow(2).getCell(2);
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
}
