# Reglas de negocio

Cada regla indica dónde está implementada y cómo se verifica.

| # | Regla | Origen | Dónde está | Prueba |
|---|-------|--------|------------|--------|
| 1 | Los precios incluyen IGV; op. gravada = total / 1.18 e IGV = total − op. gravada | SUNAT | `Comprobante` (constructor) | `comprobanteSeparaIgvDelTotal` |
| 2 | DNI de 8 dígitos; RUC de 11 que empieza en 10, 15, 17 o 20 | SUNAT | `Cliente.documentoValido` | `validaDniYRuc` |
| 3 | Factura solo para clientes con RUC; boleta para DNI | SUNAT | `ComprobanteService.emitir`, `OseSimuladoService` | Manual (P7) |
| 4 | El comprobante se emite solo cuando la venta está cancelada (saldo cero) (precondición CU08) | Empresa | `ComprobanteService.emitir` | `ComprobanteServiceTest` |
| 5 | Una venta tiene como máximo un comprobante aceptado | SUNAT | `Venta.tieneComprobanteVigente` | Manual (P7) |
| 6 | Correlativo = último de la serie + 1; serie y correlativo únicos | SUNAT | `ComprobanteRepository.ultimoCorrelativo`, restricción `UNIQUE` | BD |
| 7 | Un comprobante aceptado no se borra: se anula con nota de crédito (07) | SUNAT | `ComprobanteService.anular` | Manual (P7) |
| 8 | La cotización vale 15 días; vencida no se convierte en venta | Empresa | `CotizacionService.DIAS_VALIDEZ`, `Cotizacion.estaVencida` | Manual (P6) |
| 9 | Una cotización genera como máximo una venta, sin volver a digitar | Empresa | `Venta.desdeCotizacion`, `venta.id_cotizacion UNIQUE` | `ventaDesdeCotizacionCopiaDetalleYSaldo` |
| 10 | El total de la cotización es la suma de sus ítems | Empresa | `Cotizacion.calcularTotal` | `totalDeCotizacionSumaLosItems` |
| 11 | Un pago no supera el saldo; con saldo cero la venta pasa a PAGADA | Empresa | `Venta.registrarPago`, `Venta.calcularSaldo` | `pagosActualizanSaldoYEstado`, `noSePuedePagarMasQueElSaldo` |
| 12 | Un usuario no puede desactivarse a sí mismo | Seguridad | `UsuarioService.cambiarEstado` | Manual (P3) |
| 13 | Tras 5 intentos fallidos el usuario se bloquea 15 minutos | Seguridad | `IntentosLoginService`, `UsuarioDetailsService` | `IntentosLoginServiceTest` |
| 14 | Los DNI/RUC se enmascaran en los logs | Seguridad | `DatosSeguros.enmascararDocumento` | `DatosSegurosTest` |
| 15 | Un texto que empieza con `= + - @` no se ejecuta como fórmula al exportar | Seguridad | `DatosSeguros`, `ExcelExportador`, `CsvExportador` | `ExportadoresTest` |
| 16 | No se emite factura a un cliente con DNI ni comprobante sin pago | SUNAT / Empresa | `ComprobanteService.emitir` | `ComprobanteServiceTest` |
| 17 | Una cotización vencida o ya convertida no se convierte en venta | Empresa | `CotizacionService.convertirAVenta` | `CotizacionServiceTest` |
| 18 | Si el producto se cobra por M, ML, M2 o M3, el subtotal del ítem es precio x cantidad x metrado; las medidas son obligatorias y se aceptan como valor directo (`5`) o largo x alto (`3.20 x 0.90`) | Empresa | `Metrado.calcular`, `DetalleCotizacion` | `MetradoTest` |
| 19 | Descuento por ítem y global en porcentaje; cada porcentaje y el descuento total efectivo no pueden superar el máximo del rol (vendedor 10 %, administrador 30 %) | Empresa | `Descuento`, `CotizacionService.crear` | `DescuentoTest`, `CotizacionServiceTest` |

## Cómo se informan los errores

Las reglas lanzan `IllegalArgumentException` (dato inválido) o `IllegalStateException` (operación no permitida en el estado actual)
con un mensaje en español pensado para el usuario. Los controladores capturan esas excepciones y muestran el mensaje en la pantalla;
nunca se muestra una traza de error.
