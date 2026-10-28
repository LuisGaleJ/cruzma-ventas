package pe.cruzma.ventas.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Guarda el tipo de comprobante como '01', '03' o '07', igual que el diseño físico. */
@Converter(autoApply = true)
public class TipoComprobanteConverter implements AttributeConverter<TipoComprobante, String> {

    /** Constructor usado por JPA. */
    public TipoComprobanteConverter() { }
    /**
     * Convierte el tipo al código que se guarda en la base de datos.
     *
     * @param tipo tipo de comprobante
     * @return {@code 01}, {@code 03}, {@code 07} o {@code null}
     */
    @Override
    public String convertToDatabaseColumn(TipoComprobante tipo) {
        return tipo == null ? null : tipo.getCodigo();
    }

    /**
     * Convierte el código guardado en el tipo de comprobante.
     *
     * @param codigo código leído de la base de datos
     * @return el tipo, o {@code null}
     */
    @Override
    public TipoComprobante convertToEntityAttribute(String codigo) {
        return codigo == null ? null : TipoComprobante.desdeCodigo(codigo);
    }
}
