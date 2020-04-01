package sic.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Medida;
import sic.exception.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.MedidaRepository;
import java.util.Locale;

@ExtendWith(SpringExtension.class)
class MedidaServiceImplTest {

  @Mock MessageSource messageSource;
  @Mock MedidaRepository medidaRepository;
  @InjectMocks MedidaServiceImpl medidaService;

  private String mensaje_medida_duplicada_nombre = "Ya existe una medida con el nombre ingresado.";

  @BeforeEach
  void setup() {
    when(messageSource.getMessage("mensaje_medida_duplicada_nombre", null, Locale.getDefault()))
        .thenReturn(mensaje_medida_duplicada_nombre);
  }

  @Test
  void shouldLanzarExceptionWhenNombreDuplicadoEnAlta() {
    Medida medidaMock = new Medida();
    medidaMock.setNombre("Unidad");
    Medida medidaNueva = new Medida();
    medidaNueva.setNombre("Unidad");
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(medidaRepository.findByNombreAndEliminada("Unidad", false))
                  .thenReturn(medidaMock);
              medidaNueva.setNombre("Unidad");
              medidaService.validarOperacion(TipoDeOperacion.ALTA, medidaNueva);
            });
    assertTrue(thrown.getMessage().contains(mensaje_medida_duplicada_nombre));
  }

  @Test
  void shouldLanzarExceptionWhenNombreDuplicadoEnActualizacion() {
    Medida medida = new Medida();
    medida.setIdMedida(1L);
    medida.setNombre("Metro");
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(medidaRepository.findByNombreAndEliminada("Metro", false)).thenReturn(medida);
              Medida medidaDuplicada = new Medida();
              medidaDuplicada.setIdMedida(2L);
              medidaDuplicada.setNombre("Metro");
              medidaService.validarOperacion(TipoDeOperacion.ACTUALIZACION, medidaDuplicada);
            });
    assertTrue(thrown.getMessage().contains(mensaje_medida_duplicada_nombre));
  }
}
