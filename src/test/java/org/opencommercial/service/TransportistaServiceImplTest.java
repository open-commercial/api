package org.opencommercial.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.Localidad;
import org.opencommercial.model.Transportista;
import org.opencommercial.model.Ubicacion;
import org.opencommercial.repository.TransportistaRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {TransportistaServiceImpl.class, CustomValidator.class, MessageSource.class})
class TransportistaServiceImplTest {

  @MockBean UbicacionServiceImpl ubicacionService;
  @MockBean TransportistaRepository transportistaRepository;
  @MockBean MessageSource messageSource;

  @Autowired TransportistaServiceImpl transportistaService;

  @Test
  void shouldTestGuardarTransportista() {
    Transportista transportista = new Transportista();
    transportista.setNombre("nombre transportista");
    Localidad localidad = new Localidad();
    localidad.setIdLocalidad(1L);
    localidad.setNombre("Localidad Transportista");
    Ubicacion ubicacion = new Ubicacion();
    ubicacion.setLocalidad(localidad);
    transportista.setUbicacion(ubicacion);
    when(ubicacionService.getLocalidadPorId(1L)).thenReturn(localidad);
    transportistaService.guardar(transportista);
    verify(transportistaRepository, times(1)).save(transportista);
    when(transportistaRepository.findByNombreAndEliminado("nombre transportista", false))
        .thenReturn(transportista);
    assertThrows(BusinessServiceException.class, () -> transportistaService.guardar(transportista));
    verify(messageSource).getMessage(eq("mensaje_transportista_duplicado_nombre"), any(), any());
  }

  @Test
  void shouldTestActualizarTransportista() {
    Transportista transportista = new Transportista();
    transportista.setNombre("nombre transportista");
    transportistaService.actualizar(transportista);
    verify(transportistaRepository).save(transportista);
  }
}
