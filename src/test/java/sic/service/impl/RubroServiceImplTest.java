package sic.service.impl;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Rubro;
import sic.repository.RubroRepository;
import sic.util.CustomValidator;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, RubroServiceImpl.class, MessageSource.class})
class RubroServiceImplTest {

  @MockBean MessageSource messageSource;
  @MockBean RubroRepository rubroRepository;

  @Autowired RubroServiceImpl rubroService;

  @Test
  void shouldTestActualizarRubro() {
    Rubro rubro = new Rubro();
    rubro.setNombre("nombre rubro");
    String generatedString = RandomStringUtils.randomAlphabetic(10001);
    rubro.setImagenHtml(generatedString);
    assertThrows(
            javax.validation.ConstraintViolationException.class, () -> rubroService.actualizar(rubro));
    generatedString = RandomStringUtils.randomAlphabetic(10000);
    rubro.setImagenHtml(generatedString);
    rubroService.actualizar(rubro);
    verify(rubroRepository).save(rubro);
  }
}
