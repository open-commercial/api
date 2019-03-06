package sic.service.impl;

import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.builder.MedidaBuilder;
import sic.modelo.Medida;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.MedidaRepository;

@ExtendWith(SpringExtension.class)
class MedidaServiceImplTest {

  @Mock private MedidaRepository medidaRepository;

  @InjectMocks private MedidaServiceImpl medidaService;

  @Test
  void shouldLanzarExceptionWhenNombreVacio() {
    Medida medidaMock = new MedidaBuilder().build();
    Medida medidaNueva = new MedidaBuilder().build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(medidaRepository.findByNombreAndEmpresaAndEliminada(
                      "", medidaNueva.getEmpresa(), false))
                  .thenReturn(medidaMock);
              medidaNueva.setNombre("");
              medidaService.validarOperacion(TipoDeOperacion.ALTA, medidaNueva);
            });
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes").getString("mensaje_medida_vacio_nombre")));
  }

  @Test
  void shouldLanzarExceptionWhenNombreDuplicadoEnAlta() {
    Medida medidaMock = new MedidaBuilder().build();
    Medida medidaNueva = new MedidaBuilder().build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(medidaRepository.findByNombreAndEmpresaAndEliminada(
                      "Unidad", medidaNueva.getEmpresa(), false))
                  .thenReturn(medidaMock);
              medidaNueva.setNombre("Unidad");
              medidaService.validarOperacion(TipoDeOperacion.ALTA, medidaNueva);
            });
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes").getString("mensaje_medida_duplicada_nombre")));
  }

  @Test
  void shouldLanzarExceptionWhenNombreDuplicadoEnActualizacion() {
    Medida medidaMock = new MedidaBuilder().build();
    Medida medidaNueva = new MedidaBuilder().build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(medidaRepository.findByNombreAndEmpresaAndEliminada(
                      "Metro", medidaNueva.getEmpresa(), false))
                  .thenReturn(medidaMock);
              medidaNueva.setId_Medida(1L);
              medidaNueva.setNombre("Metro");
              when(medidaService.getMedidaPorNombre("Metro", medidaNueva.getEmpresa()))
                  .thenReturn(medidaNueva);
              Medida medidaDuplicada = new Medida();
              medidaDuplicada.setId_Medida(2L);
              medidaDuplicada.setNombre("Metro");
              medidaDuplicada.setEmpresa(medidaNueva.getEmpresa());
              medidaService.validarOperacion(TipoDeOperacion.ACTUALIZACION, medidaDuplicada);
            });
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes").getString("mensaje_medida_duplicada_nombre")));
  }
}
