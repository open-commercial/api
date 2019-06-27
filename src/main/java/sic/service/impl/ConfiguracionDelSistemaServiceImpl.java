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
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.service.IConfiguracionDelSistemaService;
import sic.repository.ConfiguracionDelSistemaRepository;
import sic.exception.BusinessServiceException;

import java.util.Locale;

@Service
@Validated
public class ConfiguracionDelSistemaServiceImpl implements IConfiguracionDelSistemaService {

  private final ConfiguracionDelSistemaRepository configuracionRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public ConfiguracionDelSistemaServiceImpl(
      ConfiguracionDelSistemaRepository configuracionRepository, MessageSource messageSource) {
    this.configuracionRepository = configuracionRepository;
    this.messageSource = messageSource;
  }

  @Override
  public ConfiguracionDelSistema getConfiguracionDelSistemaPorId(long idConfiguracionDelSistema) {
    return configuracionRepository
        .findById(idConfiguracionDelSistema)
        .orElseThrow(
            () -> new EntityNotFoundException(messageSource.getMessage(
                  "mensaje_cds_no_existente", null, Locale.getDefault())));
  }

  @Override
  public ConfiguracionDelSistema getConfiguracionDelSistemaPorEmpresa(Empresa empresa) {
    return configuracionRepository.findByEmpresa(empresa);
  }

  @Override
  @Transactional
  public ConfiguracionDelSistema guardar(@Valid ConfiguracionDelSistema cds) {
    this.validarOperacion(cds);
    cds = configuracionRepository.save(cds);
    logger.warn("La Configuracion del Sistema {} se guardó correctamente.", cds);
    return cds;
  }

  @Override
  @Transactional
  public void actualizar(@Valid ConfiguracionDelSistema cds) {
    this.validarOperacion(cds);
    if (cds.getPasswordCertificadoAfip() != null) {
      cds.setPasswordCertificadoAfip(cds.getPasswordCertificadoAfip());
    }
    if (cds.getEmailPassword() != null) {
      cds.setEmailPassword(cds.getEmailPassword());
    }
    configuracionRepository.save(cds);
  }

  @Override
  @Transactional
  public void eliminar(ConfiguracionDelSistema cds) {
    configuracionRepository.delete(cds);
  }

  @Override
  public void validarOperacion(ConfiguracionDelSistema cds) {
    if (cds.isFacturaElectronicaHabilitada()) {
      if (cds.getCertificadoAfip() == null) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cds_certificado_vacio", null, Locale.getDefault()));
      }
      if (cds.getFirmanteCertificadoAfip().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cds_firmante_vacio", null, Locale.getDefault()));
      }
      if (cds.getPasswordCertificadoAfip() == null || cds.getPasswordCertificadoAfip().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cds_password_vacio", null, Locale.getDefault()));
      }
      if (cds.getNroPuntoDeVentaAfip() <= 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cds_punto_venta_invalido", null, Locale.getDefault()));
      }
    }
    if (cds.isEmailSenderHabilitado()) {
      if (cds.getEmailUsername() == null || cds.getEmailUsername().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cds_email_vacio", null, Locale.getDefault()));
      }
      if (cds.getEmailPassword() == null || cds.getEmailPassword().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_cds_email_password_vacio", null, Locale.getDefault()));
      }
    }
  }

  @Override
  public int getCantidadMaximaDeRenglonesPorIdEmpresa(long idEmpresa) {
    return configuracionRepository.getCantidadMaximaDeRenglones(idEmpresa);
  }

  @Override
  public boolean isFacturaElectronicaHabilitada(long idEmpresa) {
    return configuracionRepository.isFacturaElectronicaHabilitada(idEmpresa);
  }
}
