package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.repository.SucursalRepository;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {SucursalServiceImpl.class, CustomValidator.class, MessageSource.class})
public class SucursalServiceImplTest {

  @MockBean SucursalRepository sucursalRepository;
  @MockBean ConfiguracionSucursalServiceImpl configuracionSucursalService;
  @MockBean UbicacionServiceImpl ubicacionService;
  @MockBean PhotoVideoUploaderImpl photoVideoUploader;
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
    sucursalService.guardar(sucursal);
    verify(sucursalRepository).save(sucursal);
    verify(productoService).guardarCantidadesDeSucursalNueva(sucursal);
  }

  @Test
  void shouldActualizarSucursal() {
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
    Sucursal sucursalPersistida = new Sucursal();
    sucursalPersistida.setIdSucursal(1L);
    sucursalPersistida.setNombre("Sucursal para guardar");
    sucursalPersistida.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    sucursalPersistida.setEmail("sucursal@delaempresa.com");
    localidad = new Localidad();
    localidad.setNombre("nombre localidad");
    localidad.setIdLocalidad(1L);
    ubicacion = new Ubicacion();
    ubicacion.setLocalidad(localidad);
    sucursalPersistida.setUbicacion(ubicacion);
    sucursalPersistida.setLogo("Logo");
    sucursalService.actualizar(sucursalParaActualizar, sucursalPersistida);
    verify(photoVideoUploader).borrarImagen("Sucursal1");
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
//    when(configuracionSucursalService.getConfiguracionSucursal(sucursal))
//        .thenReturn(configuracionSucursal);
    assertThrows(BusinessServiceException.class, () -> sucursalService.eliminar(1L));
    verify(messageSource)
        .getMessage(eq("mensaje_sucursal_no_se_puede_eliminar_predeterminada"), any(), any());
    configuracionSucursal.setPredeterminada(false);
//    when(configuracionSucursalService.getConfiguracionSucursal(sucursal))
//        .thenReturn(configuracionSucursal);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    sucursalService.eliminar(1L);
    verify(configuracionSucursalService).eliminar(configuracionSucursal);
    verify(sucursalRepository).save(sucursal);
  }
}
