package org.opencommercial.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.dto.NuevaSucursalDTO;
import org.opencommercial.repository.SucursalRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {SucursalServiceImpl.class, CustomValidator.class, MessageSource.class})
class SucursalServiceImplTest {

  @MockBean SucursalRepository sucursalRepository;
  @MockBean ConfiguracionSucursalServiceImpl configuracionSucursalService;
  @MockBean UbicacionServiceImpl ubicacionService;
  @MockBean ImageUploaderService imageUploaderService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean MessageSource messageSource;

  @Autowired SucursalServiceImpl sucursalService;

  @Test
  void shouldGetSucursalPredeterminada() {
    sucursalService.getSucursalPredeterminada();
    verify(sucursalRepository).getSucursalPredeterminada();
  }

  @Test
  void shouldGuardarSucursal() {
    NuevaSucursalDTO nuevaSucursalDTO =
            NuevaSucursalDTO.builder()
                    .nombre("Sucursal para guardar")
                    .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
                    .email("sucursal@delaempresa.com")
                    .build();
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal para guardar");
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursal.setEmail("sucursal@delaempresa.com");
    Localidad localidad = new Localidad();
    localidad.setNombre("nombre localidad");
    localidad.setIdLocalidad(1L);
    Ubicacion ubicacion = new Ubicacion();
    ubicacion.setLocalidad(localidad);
    sucursal.setUbicacion(ubicacion);
    when(ubicacionService.getLocalidadPorId(1L)).thenReturn(localidad);
    when(sucursalRepository.save(sucursal)).thenReturn(sucursal);
    sucursalService.guardar(nuevaSucursalDTO, ubicacion, null);
    verify(sucursalRepository).save(sucursal);
    verify(productoService).guardarCantidadesDeSucursalNueva(sucursal);
  }

  @Test
  void shouldActualizarSucursal() {
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    Sucursal sucursalParaActualizar = new Sucursal();
    sucursalParaActualizar.setNombre("Sucursal para guardar");
    sucursalParaActualizar.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursalParaActualizar.setEmail("sucursal@delaempresa.com");
    Localidad localidad = new Localidad();
    localidad.setNombre("nombre localidad");
    localidad.setIdLocalidad(1L);
    Ubicacion ubicacion = new Ubicacion();
    ubicacion.setLocalidad(localidad);
    sucursalParaActualizar.setUbicacion(ubicacion);
    sucursalParaActualizar.setConfiguracionSucursal(configuracionSucursal);
    Sucursal sucursalPersistida = new Sucursal();
    sucursalPersistida.setIdSucursal(1L);
    sucursalPersistida.setNombre("Sucursal para guardar");
    sucursalPersistida.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursalPersistida.setEmail("sucursal@delaempresa.com");
    sucursalPersistida.setConfiguracionSucursal(configuracionSucursal);
    localidad = new Localidad();
    localidad.setNombre("nombre localidad");
    localidad.setIdLocalidad(1L);
    ubicacion = new Ubicacion();
    ubicacion.setLocalidad(localidad);
    sucursalPersistida.setUbicacion(ubicacion);
    byte[] emptyArray = new byte[0];
    sucursalPersistida.setLogo("Logo");
    sucursalService.actualizar(sucursalParaActualizar, sucursalPersistida, emptyArray);
    verify(imageUploaderService).borrarImagen("Sucursal1");
    verify(sucursalRepository).save(sucursalParaActualizar);
  }

  @Test
  void shouldEliminarSucursal() {
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    sucursal.setNombre("Sucursal para guardar");
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursal.setEmail("sucursal@delaempresa.com");
    Localidad localidad = new Localidad();
    localidad.setNombre("nombre localidad");
    localidad.setIdLocalidad(1L);
    Ubicacion ubicacion = new Ubicacion();
    ubicacion.setLocalidad(localidad);
    sucursal.setUbicacion(ubicacion);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setPredeterminada(true);
    assertThrows(EntityNotFoundException.class, () -> sucursalService.eliminar(1L));
    verify(messageSource).getMessage(eq("mensaje_sucursal_no_existente"), any(), any());
    when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    assertThrows(BusinessServiceException.class, () -> sucursalService.eliminar(1L));
    verify(messageSource)
        .getMessage(eq("mensaje_sucursal_no_se_puede_eliminar_predeterminada"), any(), any());
    configuracionSucursal.setPredeterminada(false);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    sucursalService.eliminar(1L);
    verify(sucursalRepository).save(sucursal);
  }

  @Test
  void shouldGetSucursales() {
    Sucursal sucursal1 = new Sucursal();
    ConfiguracionSucursal configuracionSucursal1 = new ConfiguracionSucursal();
    configuracionSucursal1.setPuntoDeRetiro(true);
    sucursal1.setConfiguracionSucursal(configuracionSucursal1);
    Sucursal sucursal2 = new Sucursal();
    ConfiguracionSucursal configuracionSucursal2 = new ConfiguracionSucursal();
    sucursal2.setConfiguracionSucursal(configuracionSucursal2);
    Sucursal sucursal3 = new Sucursal();
    ConfiguracionSucursal configuracionSucursal3 = new ConfiguracionSucursal();
    configuracionSucursal3.setPuntoDeRetiro(true);
    sucursal3.setConfiguracionSucursal(configuracionSucursal3);
    List<Sucursal> sucursales = new ArrayList<>();
    sucursales.add(sucursal1);
    sucursales.add(sucursal2);
    sucursales.add(sucursal3);
    when(sucursalRepository.findAllByAndEliminadaOrderByNombreAsc(false)).thenReturn(sucursales);
    assertEquals(2, sucursalService.getSucusales(true).size());
    assertEquals(3, sucursalService.getSucusales(false).size());
  }

  @Test
  void shouldNotValidarReglasDeNegocio() {
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal Test");
    sucursal.setIdSucursal(1L);
    sucursal.setIdFiscal(123L);
    Sucursal sucursalDuplicada = new Sucursal();
    sucursalDuplicada.setIdSucursal(2L);
    sucursalDuplicada.setNombre("Sucursal Test");
    sucursalDuplicada.setIdFiscal(123L);
    when(sucursalRepository.findByNombreIsAndEliminadaOrderByNombreAsc("Sucursal Test", false)).thenReturn(sucursalDuplicada);
    assertThrows(
            BusinessServiceException.class,
            () -> sucursalService.validarReglasDeNegocio(TipoDeOperacion.ALTA, sucursal));
    assertThrows(
            BusinessServiceException.class,
            () -> sucursalService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, sucursal));
    verify(messageSource, times(2))
            .getMessage(
                    eq("mensaje_sucursal_duplicado_nombre"),
                    any(),
                    eq(Locale.getDefault()));
    sucursalDuplicada.setNombre("Otro nombre");
    sucursalDuplicada.setIdSucursal(2L);
    when(sucursalRepository.findByNombreIsAndEliminadaOrderByNombreAsc("Sucursal Test", false)).thenReturn(null);
    /* Momentaneamente se anula la validación por ID fiscal duplicado
    when(sucursalRepository.findByIdFiscalAndEliminada(123L, false)).thenReturn(sucursalDuplicada);
    assertThrows(
            BusinessServiceException.class,
            () ->
                    sucursalService.validarReglasDeNegocio(TipoDeOperacion.ALTA , sucursal));
    sucursalDuplicada.setIdSucursal(2L);
    when(sucursalRepository.findByIdFiscalAndEliminada(123L, false)).thenReturn(sucursalDuplicada);
    assertThrows(
            BusinessServiceException.class,
            () ->
                    sucursalService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION , sucursal));
    verify(messageSource, times(2))
            .getMessage(
                    eq("mensaje_sucursal_duplicado_cuip"),
                    any(),
                    eq(Locale.getDefault()));
    */
    when(sucursalRepository.findByIdFiscalAndEliminada(123L, false)).thenReturn(null);
    Ubicacion ubicacion = new Ubicacion();
    sucursal.setUbicacion(ubicacion);
    assertThrows(
            BusinessServiceException.class,
            () -> sucursalService.validarReglasDeNegocio(TipoDeOperacion.ALTA , sucursal));
    verify(messageSource)
            .getMessage(
                    eq("mensaje_ubicacion_sin_localidad"),
                    any(),
                    eq(Locale.getDefault()));
    ubicacion.setLocalidad(new Localidad());
    sucursal.setUbicacion(ubicacion);
    assertThrows(
            BusinessServiceException.class,
            () -> sucursalService.validarReglasDeNegocio(TipoDeOperacion.ALTA , sucursal));
    verify(messageSource)
            .getMessage(
                    eq("mensaje_sucursal_sin_configuracion"),
                    any(),
                    eq(Locale.getDefault()));
  }
}
