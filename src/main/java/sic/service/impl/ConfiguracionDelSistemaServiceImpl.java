package sic.service.impl;

import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.modelo.TipoDeOperacion;
import sic.service.IConfiguracionDelSistemaService;
import sic.repository.ConfiguracionDelSistemaRepository;
import sic.service.BusinessServiceException;

@Service
public class ConfiguracionDelSistemaServiceImpl implements IConfiguracionDelSistemaService {

    private final ConfiguracionDelSistemaRepository configuracionRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ConfiguracionDelSistemaServiceImpl(ConfiguracionDelSistemaRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    public ConfiguracionDelSistema getConfiguracionDelSistemaPorId(long idConfiguracionDelSistema) {
        ConfiguracionDelSistema cds = configuracionRepository.findOne(idConfiguracionDelSistema);
        if (cds == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cds_no_existente"));
        }
        return cds;
    }

    @Override
    public ConfiguracionDelSistema getConfiguracionDelSistemaPorEmpresa(Empresa empresa) {
        return configuracionRepository.findByEmpresa(empresa);
    }

    @Override
    @Transactional
    public ConfiguracionDelSistema guardar(ConfiguracionDelSistema cds) {
        this.validarCds(TipoDeOperacion.ALTA, cds);
        cds = configuracionRepository.save(cds);
        logger.warn("La Configuracion del Sistema {} se guardó correctamente.", cds);
        return cds;
    }

    @Override
    @Transactional
    public void actualizar(ConfiguracionDelSistema cds) {
        this.validarCds(TipoDeOperacion.ACTUALIZACION, cds);
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
    public void validarCds(TipoDeOperacion tipoOperacion, ConfiguracionDelSistema cds) {
        if (tipoOperacion.equals(TipoDeOperacion.ACTUALIZACION)) {
            if (cds.isFacturaElectronicaHabilitada() || cds.isEmailSenderHabilitado()) {
                ConfiguracionDelSistema cdsRecuperado =
                        this.getConfiguracionDelSistemaPorId(cds.getId_ConfiguracionDelSistema());
                if (cds.isFacturaElectronicaHabilitada() && cds.getPasswordCertificadoAfip().equals("")) {
                    cds.setPasswordCertificadoAfip(cdsRecuperado.getPasswordCertificadoAfip());
                }
                if (cds.isEmailSenderHabilitado() && cds.getEmailPassword().equals("")) {
                    cds.setEmailPassword(cdsRecuperado.getEmailPassword());
                }
            }
        } else if (tipoOperacion.equals(TipoDeOperacion.ALTA)
                && cds.isEmailSenderHabilitado()
                && !cds.getEmailPassword().equals("")) {
            cds.setEmailPassword(cds.getEmailPassword());
        }
        if (cds.isFacturaElectronicaHabilitada()) {
            if (cds.getCertificadoAfip() == null) {
                throw new BusinessServiceException(
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_certificado_vacio"));
            }
            if (cds.getFirmanteCertificadoAfip().isEmpty()) {
                throw new BusinessServiceException(
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_firmante_vacio"));
            }
            if (cds.getPasswordCertificadoAfip() == null || cds.getPasswordCertificadoAfip().isEmpty()) {
                throw new BusinessServiceException(
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_password_vacio"));
            }
            if (cds.getNroPuntoDeVentaAfip() <= 0) {
                throw new BusinessServiceException(
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_punto_venta_invalido"));
            }
        }
        if (cds.isEmailSenderHabilitado()) {
            if (cds.getEmailUsername() == null || cds.getEmailUsername().isEmpty()) {
                throw new BusinessServiceException(
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_email_vacio"));
            }
            if (cds.getEmailPassword() == null || cds.getEmailPassword().isEmpty()) {
                throw new BusinessServiceException(
                        ResourceBundle.getBundle("Mensajes").getString("mensaje_cds_email_password_vacio"));
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
