package sic.service.impl;

import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.service.IConfiguracionDelSistemaService;
import sic.repository.ConfiguracionDelSistemaRepository;
import sic.service.BusinessServiceException;

@Service
@Validated
public class ConfiguracionDelSistemaServiceImpl implements IConfiguracionDelSistemaService {

  private final ConfiguracionDelSistemaRepository configuracionRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public ConfiguracionDelSistemaServiceImpl(
      ConfiguracionDelSistemaRepository configuracionRepository) {
    this.configuracionRepository = configuracionRepository;
  }

  @Override
  public ConfiguracionDelSistema getConfiguracionDelSistemaPorId(long idConfiguracionDelSistema) {
    return configuracionRepository
        .findById(idConfiguracionDelSistema)
        .orElseThrow(
            () ->
                new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_cds_no_existente")));
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
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_cds_certificado_vacio"));
      }
      if (cds.getFirmanteCertificadoAfip().isEmpty()) {
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_cds_firmante_vacio"));
      }
      if (cds.getPasswordCertificadoAfip() == null || cds.getPasswordCertificadoAfip().isEmpty()) {
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_cds_password_vacio"));
      }
      if (cds.getNroPuntoDeVentaAfip() <= 0) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_cds_punto_venta_invalido"));
      }
    }
    if (cds.isEmailSenderHabilitado()) {
      if (cds.getEmailUsername() == null || cds.getEmailUsername().isEmpty()) {
        throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_cds_email_vacio"));
      }
      if (cds.getEmailPassword() == null || cds.getEmailPassword().isEmpty()) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_cds_email_password_vacio"));
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
