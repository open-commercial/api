package org.opencommercial.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.Medida;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.repository.MedidaRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {MedidaServiceImpl.class, CustomValidator.class, MessageSource.class})
class MedidaServiceImplTest {

  @MockBean MessageSource messageSource;
  @MockBean MedidaRepository medidaRepository;

  @Autowired MedidaServiceImpl medidaService;

  @Test
  void shouldLanzarExceptionWhenNombreDuplicadoEnAlta() {
    Medida medidaMock = new Medida();
    medidaMock.setNombre("Unidad");
    Medida medidaNueva = new Medida();
    medidaNueva.setNombre("Unidad");
    when(medidaRepository.findByNombreAndEliminada("Unidad", false)).thenReturn(medidaMock);
    medidaNueva.setNombre("Unidad");
    assertThrows(
        BusinessServiceException.class,
        () -> medidaService.validarReglasDeNegocio(TipoDeOperacion.ALTA, medidaNueva));
    verify(messageSource).getMessage(eq("mensaje_medida_duplicada_nombre"), any(), any());
  }

  @Test
  void shouldLanzarExceptionWhenNombreDuplicadoEnActualizacion() {
    Medida medida = new Medida();
    medida.setIdMedida(1L);
    medida.setNombre("Metro");
    when(medidaRepository.findByNombreAndEliminada("Metro", false)).thenReturn(medida);
    Medida medidaDuplicada = new Medida();
    medidaDuplicada.setIdMedida(2L);
    medidaDuplicada.setNombre("Metro");
    assertThrows(
        BusinessServiceException.class,
        () -> medidaService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, medidaDuplicada));
    verify(messageSource).getMessage(eq("mensaje_medida_duplicada_nombre"), any(), any());
  }

  @Test
  void shouldActualizarMedida() {
      Medida medida = new Medida();
      medida.setNombre("Metro");
      medidaService.actualizar(medida);
      verify(medidaRepository).save(medida);
  }
}
