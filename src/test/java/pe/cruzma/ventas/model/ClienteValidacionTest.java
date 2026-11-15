package pe.cruzma.ventas.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Validación del teléfono del cliente: opcional, pero solo dígitos (observación del docente). */
class ClienteValidacionTest {

    private static Validator validator;

    @BeforeAll
    static void preparar() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    private static Cliente cliente(String telefono) {
        Cliente c = new Cliente();
        c.setTipoDoc(TipoDocumento.DNI);
        c.setNumDoc("45871236");
        c.setRazonSocial("María Castillo Ruiz");
        c.setTelefono(telefono);
        return c;
    }

    private static long erroresDeTelefono(Cliente c) {
        return validator.validate(c).stream()
                .filter(v -> v.getPropertyPath().toString().equals("telefono")).count();
    }

    @Test
    void aceptaCelularFijoConCodigoYVacio() {
        assertEquals(0, erroresDeTelefono(cliente("969458712")));
        assertEquals(0, erroresDeTelefono(cliente("073305511")));
        assertEquals(0, erroresDeTelefono(cliente("305511")));
        assertEquals(0, erroresDeTelefono(cliente("")));
        assertEquals(0, erroresDeTelefono(cliente(null)));
    }

    @Test
    void rechazaLetrasSimbolosYLargoIncorrecto() {
        assertTrue(erroresDeTelefono(cliente("bbb")) > 0);
        assertTrue(erroresDeTelefono(cliente("969-458-712")) > 0);
        assertTrue(erroresDeTelefono(cliente("12345")) > 0);
        assertTrue(erroresDeTelefono(cliente("9694587120")) > 0);
    }
}
