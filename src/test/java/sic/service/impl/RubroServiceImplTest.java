package sic.service.impl;

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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

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
  /*  byte[] array = new byte[10001]; // length is bounded by 7
    new Random().nextBytes(array);
    String generatedString = new String(array, StandardCharsets.UTF_8);
    rubro.setImagenHtml(generatedString);*/
    rubroService.actualizar(rubro);
    verify(rubroRepository).save(rubro);
  }
}
