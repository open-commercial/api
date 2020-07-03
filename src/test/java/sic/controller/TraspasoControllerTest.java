package sic.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.service.impl.TraspasoServiceImpl;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TraspasoController.class})
public class TraspasoControllerTest {

  @MockBean TraspasoServiceImpl traspasoService;

  @Autowired TraspasoController traspasoController;

  @Test
  void shouldBuscarTraspasos() {
    BusquedaTraspasoCriteria traspasoCriteria = BusquedaTraspasoCriteria.builder().build();
    traspasoController.getTraspasosCriteria(traspasoCriteria);
    verify(traspasoService).buscarTraspasos(traspasoCriteria);
  }

  @Test
  void shouldGetRenglones() {
    traspasoController.getRenglonesDelTraspaso(1L);
    verify(traspasoService).getRenglonesTraspaso(1L);
  }
}
