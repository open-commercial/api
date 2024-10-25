package sic.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.Localidad;
import sic.modelo.Provincia;
import sic.modelo.Ubicacion;
import sic.modelo.dto.LocalidadesParaActualizarDTO;
import sic.repository.LocalidadRepository;
import sic.repository.ProvinciaRepository;
import sic.repository.UbicacionRepository;
import sic.util.CustomValidator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {UbicacionServiceImpl.class, CustomValidator.class, MessageSource.class})
class UbicacionServiceImplTest {

  @MockBean MessageSource messageSource;
  @MockBean UbicacionRepository UbicacionRepository;
  @MockBean LocalidadRepository localidadRepository;
  @MockBean ProvinciaRepository provinciaRepository;

  @Autowired UbicacionServiceImpl ubicacionService;

  @Test
  void shouldGuardarUbicacion() {
    Ubicacion ubicacion = new Ubicacion();
    Localidad localidad = new Localidad();
    ubicacion.setLocalidad(localidad);
    ubicacion.setLatitud(Double.valueOf("80"));
    ubicacion.setLongitud(Double.valueOf("120"));
    when(UbicacionRepository.save(ubicacion)).thenReturn(ubicacion);
    assertEquals(ubicacion, ubicacionService.guardar(ubicacion));
  }

  @Test
  void shouldActualizarLocalidad() {
    Localidad localidad = new Localidad();
    assertThrows(
        BusinessServiceException.class, () -> ubicacionService.actualizarLocalidad(localidad));
    verify(messageSource).getMessage(eq("mensaje_localidad_vacio_nombre"), any(), any());
    localidad.setNombre("nombre localidad");
    assertThrows(
        BusinessServiceException.class, () -> ubicacionService.actualizarLocalidad(localidad));
    verify(messageSource).getMessage(eq("mensaje_localidad_provincia_vacio"), any(), any());
    Provincia provincia = new Provincia();
    localidad.setProvincia(provincia);
    localidad.setNombre("nombre localidad");
    Localidad localidadDuplicada = new Localidad();
    localidadDuplicada.setProvincia(provincia);
    localidadDuplicada.setNombre("nombre localidad");
    localidadDuplicada.setIdLocalidad(1L);
    when(localidadRepository.findByNombreAndProvinciaOrderByNombreAsc(
            "nombre localidad", provincia))
        .thenReturn(localidadDuplicada);
    assertThrows(
        BusinessServiceException.class, () -> ubicacionService.actualizarLocalidad(localidad));
    verify(messageSource).getMessage(eq("mensaje_localidad_duplicado_nombre"), any(), any());
  }

  @Test
  void shouldActualizarMultiplesLocalidades() {
    Localidad localidad1 = new Localidad();
    localidad1.setIdLocalidad(1L);
    localidad1.setNombre("Corrientes");
    Localidad localidad2 = new Localidad();
    localidad2.setIdLocalidad(2L);
    localidad2.setNombre("Misiones");
    Provincia provincia = new Provincia();
    localidad1.setProvincia(provincia);
    localidad2.setProvincia(provincia);
    LocalidadesParaActualizarDTO localidadesParaActualizar = LocalidadesParaActualizarDTO.builder()
            .idLocalidad(new long[]{1L, 2L})
            .costoDeEnvio(BigDecimal.TEN)
            .envioGratuito(true)
            .build();
    when(localidadRepository.findById(1L)).thenReturn(localidad1);
    when(localidadRepository.findById(2L)).thenReturn(localidad2);
    ubicacionService.actualizarMultiplesLocalidades(localidadesParaActualizar);
    verify(localidadRepository).save(localidad1);
    verify(localidadRepository).save(localidad2);
  }

}
