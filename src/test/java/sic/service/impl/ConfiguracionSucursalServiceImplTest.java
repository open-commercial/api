package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;
import sic.repository.ConfiguracionSucursalRepository;
import sic.util.CustomValidator;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ConfiguracionSucursalServiceImpl.class, CustomValidator.class, MessageSource.class})
public class ConfiguracionSucursalServiceImplTest {

  @MockBean ConfiguracionSucursalRepository configuracionSucursalRepository;
  @MockBean MessageSource messageSource;

  @Autowired ConfiguracionSucursalServiceImpl configuracionSucursalService;

  @Test
  void shouldGuardarSucursal() {
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setVencimientoCorto(1L);
    configuracionSucursal.setVencimientoLargo(5L);
    configuracionSucursal.setFacturaElectronicaHabilitada(true);
    assertThrows(
        BusinessServiceException.class,
        () -> configuracionSucursalService.guardar(configuracionSucursal));
    verify(messageSource).getMessage(eq("mensaje_sucursal_certificado_vacio"), any(), any());
    configuracionSucursal.setCertificadoAfip(("").getBytes());
    configuracionSucursal.setFirmanteCertificadoAfip("");
    assertThrows(
        BusinessServiceException.class,
        () -> configuracionSucursalService.guardar(configuracionSucursal));
    verify(messageSource).getMessage(eq("mensaje_sucursal_firmante_vacio"), any(), any());
    configuracionSucursal.setFirmanteCertificadoAfip("firmante");
    assertThrows(
        BusinessServiceException.class,
        () -> configuracionSucursalService.guardar(configuracionSucursal));
    verify(messageSource).getMessage(eq("mensaje_sucursal_password_vacio"), any(), any());
    configuracionSucursal.setPasswordCertificadoAfip("password");
    assertThrows(
        BusinessServiceException.class,
        () -> configuracionSucursalService.guardar(configuracionSucursal));
    verify(messageSource).getMessage(eq("mensaje_sucursal_punto_venta_invalido"), any(), any());
    configuracionSucursal.setNroPuntoDeVentaAfip(3);
    configuracionSucursal.setPredeterminada(true);
    configuracionSucursalService.guardar(configuracionSucursal);
    verify(configuracionSucursalRepository).desmarcarSucursalPredeterminada();
    verify(configuracionSucursalRepository).save(configuracionSucursal);
  }

  @Test
  void shouldActualizarSucursal() {
    ConfiguracionSucursal configuracionSucursalPersistida = new ConfiguracionSucursal();
    ConfiguracionSucursal configuracionDeSucursalParaActualizar = new ConfiguracionSucursal();
    configuracionSucursalPersistida.setPredeterminada(true);
    assertThrows(
        BusinessServiceException.class,
        () ->
            configuracionSucursalService.actualizar(
                configuracionSucursalPersistida, configuracionDeSucursalParaActualizar));
    verify(messageSource).getMessage(eq("mensaje_sucursal_quitar_predeterminada"), any(), any());
    Sucursal sucursal = new Sucursal();
    configuracionSucursalPersistida.setSucursal(sucursal);
    configuracionSucursalPersistida.setPredeterminada(false);
    configuracionDeSucursalParaActualizar.setFacturaElectronicaHabilitada(true);
    configuracionDeSucursalParaActualizar.setPasswordCertificadoAfip("");
    configuracionSucursalPersistida.setPasswordCertificadoAfip("password");
    configuracionSucursalPersistida.setCertificadoAfip(("").getBytes());
    configuracionSucursalPersistida.setSignTokenWSAA("firmaToken");
    configuracionSucursalPersistida.setTokenWSAA("token");
    configuracionSucursalPersistida.setFechaGeneracionTokenWSAA(LocalDateTime.MIN);
    configuracionSucursalPersistida.setFechaVencimientoTokenWSAA(LocalDateTime.MIN);
    configuracionDeSucursalParaActualizar.setVencimientoLargo(0L);
    configuracionDeSucursalParaActualizar.setVencimientoCorto(0L);
    configuracionSucursalPersistida.setVencimientoCorto(1L);
    configuracionSucursalPersistida.setVencimientoLargo(1L);
    configuracionDeSucursalParaActualizar.setFirmanteCertificadoAfip("firmante");
    configuracionDeSucursalParaActualizar.setNroPuntoDeVentaAfip(3);
    configuracionSucursalService.actualizar(
        configuracionSucursalPersistida, configuracionDeSucursalParaActualizar);
    verify(configuracionSucursalRepository).save(configuracionDeSucursalParaActualizar);
  }
}
