package org.opencommercial.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.ConfiguracionSucursal;
import org.opencommercial.repository.ConfiguracionSucursalRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Slf4j
public class ConfiguracionSucursalServiceImpl implements ConfiguracionSucursalService {

  private final ConfiguracionSucursalRepository configuracionRepository;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public ConfiguracionSucursalServiceImpl(
    ConfiguracionSucursalRepository configuracionRepository,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.configuracionRepository = configuracionRepository;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public ConfiguracionSucursal getConfiguracionSucursalPorId(long idConfiguracionSucursal) {
    return configuracionRepository
        .findById(idConfiguracionSucursal)
        .orElseThrow(
            () -> new EntityNotFoundException(messageSource.getMessage(
                  "mensaje_sucursal_no_existente", null, Locale.getDefault())));
  }

  @Override
  @Transactional
  public ConfiguracionSucursal guardar(ConfiguracionSucursal configuracionSucursal) {
    customValidator.validar(configuracionSucursal);
    this.validarReglasDeNegocio(configuracionSucursal);
    if (configuracionSucursal.isPredeterminada()) {
      configuracionRepository.desmarcarSucursalPredeterminada();
    }
    configuracionSucursal = configuracionRepository.save(configuracionSucursal);
    log.info("La configuracion de sucursal {} se guardó correctamente.", configuracionSucursal);
    return configuracionSucursal;
  }

  @Override
  @Transactional
  public void actualizar(ConfiguracionSucursal configuracionDeSucursalParaActualizar) {
    ConfiguracionSucursal configuracionSucursalPersistida =
        this.getConfiguracionSucursalPorId(
            configuracionDeSucursalParaActualizar.getIdConfiguracionSucursal());
    if (!configuracionDeSucursalParaActualizar.isPredeterminada()
        && configuracionSucursalPersistida.isPredeterminada()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_sucursal_quitar_predeterminada", null, Locale.getDefault()));
    }
    if (configuracionDeSucursalParaActualizar.isFacturaElectronicaHabilitada()) {
      if (configuracionDeSucursalParaActualizar.getPasswordCertificadoAfip().equals("")) {
        configuracionDeSucursalParaActualizar.setPasswordCertificadoAfip(
            configuracionSucursalPersistida.getPasswordCertificadoAfip());
      }
      if (configuracionDeSucursalParaActualizar.getCertificadoAfip() == null) {
        configuracionDeSucursalParaActualizar.setCertificadoAfip(
            configuracionSucursalPersistida.getCertificadoAfip());
      }
      if (configuracionDeSucursalParaActualizar.getSignTokenWSAA() == null) {
        configuracionDeSucursalParaActualizar.setSignTokenWSAA(
            configuracionSucursalPersistida.getSignTokenWSAA());
      }
      if (configuracionDeSucursalParaActualizar.getTokenWSAA() == null) {
        configuracionDeSucursalParaActualizar.setTokenWSAA(
            configuracionSucursalPersistida.getTokenWSAA());
      }
      if (configuracionDeSucursalParaActualizar.getFechaGeneracionTokenWSAA() == null) {
        configuracionDeSucursalParaActualizar.setFechaGeneracionTokenWSAA(
            configuracionSucursalPersistida.getFechaGeneracionTokenWSAA());
      }
      if (configuracionDeSucursalParaActualizar.getFechaVencimientoTokenWSAA() == null) {
        configuracionDeSucursalParaActualizar.setFechaVencimientoTokenWSAA(
            configuracionSucursalPersistida.getFechaVencimientoTokenWSAA());
      }
    }
    if (configuracionDeSucursalParaActualizar.getVencimientoLargo() == 0L) {
      configuracionDeSucursalParaActualizar.setVencimientoLargo(
          configuracionSucursalPersistida.getVencimientoLargo());
    }
    if (configuracionDeSucursalParaActualizar.getVencimientoCorto() == 0L) {
      configuracionDeSucursalParaActualizar.setVencimientoCorto(
              configuracionSucursalPersistida.getVencimientoCorto());
    }
    customValidator.validar(configuracionDeSucursalParaActualizar);
    this.validarReglasDeNegocio(configuracionDeSucursalParaActualizar);
    if (!configuracionSucursalPersistida.isPredeterminada()
        && configuracionDeSucursalParaActualizar.isPredeterminada()) {
      configuracionRepository.desmarcarSucursalPredeterminada();
    }
    if (configuracionDeSucursalParaActualizar.getPasswordCertificadoAfip() == null) {
      configuracionDeSucursalParaActualizar.setPasswordCertificadoAfip(
          configuracionSucursalPersistida.getPasswordCertificadoAfip());
    }
    configuracionRepository.save(configuracionDeSucursalParaActualizar);
  }

  @Override
  @Transactional
  public void eliminar(ConfiguracionSucursal configuracionSucursal) {
    configuracionRepository.delete(configuracionSucursal);
  }

  @Override
  public void validarReglasDeNegocio(ConfiguracionSucursal configuracionSucursal) {
    if (configuracionSucursal.isFacturaElectronicaHabilitada()) {
      if (configuracionSucursal.getCertificadoAfip() == null) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_sucursal_certificado_vacio", null, Locale.getDefault()));
      }
      if (configuracionSucursal.getFirmanteCertificadoAfip().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_sucursal_firmante_vacio", null, Locale.getDefault()));
      }
      if (configuracionSucursal.getPasswordCertificadoAfip() == null || configuracionSucursal.getPasswordCertificadoAfip().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_sucursal_password_vacio", null, Locale.getDefault()));
      }
      if (configuracionSucursal.getNroPuntoDeVentaAfip() <= 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_sucursal_punto_venta_invalido", null, Locale.getDefault()));
      }
    }
  }

  @Override
  public int getCantidadMaximaDeRenglonesPorIdSucursal(long idSucursal) {
    return configuracionRepository.getCantidadMaximaDeRenglones(idSucursal);
  }

  @Override
  public boolean isFacturaElectronicaHabilitada(long idSucursal) {
    return configuracionRepository.isFacturaElectronicaHabilitada(idSucursal);
  }
}
