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
}
