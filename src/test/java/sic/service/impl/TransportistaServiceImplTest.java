package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.Localidad;
import sic.modelo.Transportista;
import sic.modelo.Ubicacion;
import sic.repository.TransportistaRepository;
import sic.util.CustomValidator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {TransportistaServiceImpl.class, CustomValidator.class, MessageSource.class})
public class TransportistaServiceImplTest {

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
}
