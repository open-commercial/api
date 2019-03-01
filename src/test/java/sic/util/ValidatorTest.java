package sic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ValidatorTest {

  @Test
  void shouldEsVacioWhenNoEsVacio() {
    String campo = "no Vacio";
    boolean result = Validator.esVacio(campo);
    assertFalse(result);
  }

  @Test
  void shouldEsEmailValidoWhenNoLoEs() {
    String cadena = "No es un mail Valido";
    boolean result = Validator.esEmailValido(cadena);
    assertFalse(result);
  }
}
