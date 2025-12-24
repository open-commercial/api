package org.opencommercial.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.opencommercial.model.dto.LocalidadesParaActualizarDTO;
import org.opencommercial.service.UbicacionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UbicacionController.class})
class UbicacionControllerTest {

  @MockitoBean UbicacionServiceImpl ubicacionService;
  @MockitoBean ModelMapper modelMapper;

  @Autowired UbicacionController ubicacionController;

  @Test
  void shouldActualizarMultiplesLocalidades() {
    LocalidadesParaActualizarDTO localidadesParaActualizarDTO =
        LocalidadesParaActualizarDTO.builder().build();
    ubicacionController.actualizarMultiplesUbicaciones(localidadesParaActualizarDTO);
    verify(ubicacionService).actualizarMultiplesLocalidades(localidadesParaActualizarDTO);
  }
}
