package pe.cruzma.ventas.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Contraseña inicial: en local se acepta la de demostración; en internet, no. */
class DataInitializerTest {

    @Test
    void enLocalAceptaLaClaveDeDemostracion() {
        assertDoesNotThrow(() -> DataInitializer.validarClaveInicial(false, DataInitializer.CLAVE_DEMO));
    }

    @Test
    void enInternetRechazaLaClaveDeDemostracion() {
        assertThrows(IllegalStateException.class,
                () -> DataInitializer.validarClaveInicial(true, DataInitializer.CLAVE_DEMO));
    }

    @Test
    void enInternetRechazaUnaClaveCortaOVacia() {
        assertThrows(IllegalStateException.class, () -> DataInitializer.validarClaveInicial(true, "corta123"));
        assertThrows(IllegalStateException.class, () -> DataInitializer.validarClaveInicial(true, ""));
        assertThrows(IllegalStateException.class, () -> DataInitializer.validarClaveInicial(true, null));
    }

    @Test
    void enInternetAceptaUnaClavePropiaLarga() {
        assertDoesNotThrow(() -> DataInitializer.validarClaveInicial(true, "Piura-Inox-2026!"));
    }
}
