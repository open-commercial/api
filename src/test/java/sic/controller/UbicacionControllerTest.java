package sic.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.dto.LocalidadesParaActualizarDTO;
import sic.service.impl.UbicacionServiceImpl;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UbicacionController.class})
class UbicacionControllerTest {

  @MockBean UbicacionServiceImpl ubicacionService;
  @MockBean ModelMapper modelMapper;

  @Autowired UbicacionController ubicacionController;

  @Test
  void shouldActualizarMultiplesLocalidades() {
    LocalidadesParaActualizarDTO localidadesParaActualizarDTO =
        LocalidadesParaActualizarDTO.builder().build();
    ubicacionController.actualizarMultiplesUbicaciones(localidadesParaActualizarDTO);
    verify(ubicacionService).actualizarMultiplesLocalidades(localidadesParaActualizarDTO);
  }
}
