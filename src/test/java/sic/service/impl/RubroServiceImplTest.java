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

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CustomValidator.class, RubroServiceImpl.class, MessageSource.class})
public class RubroServiceImplTest {

  @MockBean MessageSource messageSource;
  @MockBean RubroRepository rubroRepository;

  @Autowired RubroServiceImpl rubroService;

  @Test
  void shouldTestActualizarRubro() {
    Rubro rubro = new Rubro();
    rubro.setNombre("nombre rubro");
    rubroService.actualizar(rubro);
    verify(rubroRepository).save(rubro);
  }
}
