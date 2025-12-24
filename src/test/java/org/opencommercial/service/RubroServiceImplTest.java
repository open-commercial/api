package org.opencommercial.service;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.model.Rubro;
import org.opencommercial.repository.RubroRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, RubroServiceImpl.class, MessageSource.class})
class RubroServiceImplTest {

  @MockitoBean MessageSource messageSource;
  @MockitoBean RubroRepository rubroRepository;

  @Autowired RubroServiceImpl rubroService;

  @Test
  void shouldTestActualizarRubro() {
    Rubro rubro = new Rubro();
    rubro.setNombre("nombre rubro");
    String generatedString = RandomStringUtils.randomAlphabetic(10001);
    rubro.setImagenHtml(generatedString);
    assertThrows(
            jakarta.validation.ConstraintViolationException.class, () -> rubroService.actualizar(rubro));
    generatedString = RandomStringUtils.randomAlphabetic(10000);
    rubro.setImagenHtml(generatedString);
    rubroService.actualizar(rubro);
    verify(rubroRepository).save(rubro);
  }
}
