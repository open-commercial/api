package sic.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidatorTest {

    @Test
    public void shouldEsVacioWhenNoEsVacio() {
        String campo = "no Vacio";
        boolean result = Validator.esVacio(campo);
        assertFalse(result);
    }

    @Test
    public void shouldEsEmailValidoWhenNoLoEs() {
        String cadena = "No es un mail Valido";
        boolean result = Validator.esEmailValido(cadena);
        assertFalse(result);
    }
}
