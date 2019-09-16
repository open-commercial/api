package sic.service.impl;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;
import sic.service.IConfiguracionSucursalService;
import sic.repository.ConfiguracionSucursalRepository;
import sic.exception.BusinessServiceException;

import java.util.Locale;

@Service
@Validated
public class ConfiguracionSucursalServiceImpl implements IConfiguracionSucursalService {

  private final ConfiguracionSucursalRepository configuracionRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public ConfiguracionSucursalServiceImpl(
    ConfiguracionSucursalRepository configuracionRepository, MessageSource messageSource) {
    this.configuracionRepository = configuracionRepository;
    this.messageSource = messageSource;
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
  public ConfiguracionSucursal getConfiguracionDelSucursal(Sucursal sucursal) {
    return configuracionRepository.findBySucursal(sucursal);
  }

  @Override
  @Transactional
  public ConfiguracionSucursal guardar(@Valid ConfiguracionSucursal configuracionSucursal) {
    this.validarOperacion(configuracionSucursal);
    configuracionSucursal = configuracionRepository.save(configuracionSucursal);
    logger.warn("La Configuracion del Sistema {} se guardó correctamente.", configuracionSucursal);
    return configuracionSucursal;
  }

  @Override
  @Transactional
  public void actualizar(@Valid ConfiguracionSucursal configuracionSucursal) {
    this.validarOperacion(configuracionSucursal);
    if (configuracionSucursal.getPasswordCertificadoAfip() != null) {
      configuracionSucursal.setPasswordCertificadoAfip(configuracionSucursal.getPasswordCertificadoAfip());
    }
    configuracionRepository.save(configuracionSucursal);
  }

  @Override
  @Transactional
  public void eliminar(ConfiguracionSucursal configuracionSucursal) {
    configuracionRepository.delete(configuracionSucursal);
  }

  @Override
  public void validarOperacion(ConfiguracionSucursal configuracionSucursal) {
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
