package sic.service.impl;

import java.util.List;
import java.util.Locale;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.service.*;
import sic.modelo.TipoDeOperacion;
import sic.repository.EmpresaRepository;
import sic.exception.BusinessServiceException;

@Service
public class EmpresaServiceImpl implements IEmpresaService {

  private final EmpresaRepository empresaRepository;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final IPhotoVideoUploader photoVideoUploader;
  private final IUbicacionService ubicacionService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public EmpresaServiceImpl(
      EmpresaRepository empresaRepository,
      IConfiguracionDelSistemaService configuracionDelSistemaService,
      IUbicacionService ubicacionService,
      IPhotoVideoUploader photoVideoUploader,
      MessageSource messageSource) {
    this.empresaRepository = empresaRepository;
    this.configuracionDelSistemaService = configuracionDelSistemaService;
    this.ubicacionService = ubicacionService;
    this.photoVideoUploader = photoVideoUploader;
    this.messageSource = messageSource;
  }

  @Override
  public Empresa getEmpresaPorId(Long idEmpresa) {
    return empresaRepository
        .findById(idEmpresa)
        .orElseThrow(
            () ->
                new EntityNotFoundException(messageSource.getMessage(
                  "mensaje_empresa_no_existente", null, Locale.getDefault())));
  }

  @Override
  public List<Empresa> getEmpresas() {
    return empresaRepository.findAllByAndEliminadaOrderByNombreAsc(false);
  }

  @Override
  public Empresa getEmpresaPorNombre(String nombre) {
    return empresaRepository.findByNombreIsAndEliminadaOrderByNombreAsc(nombre, false);
  }

  @Override
  public Empresa getEmpresaPorIdFiscal(Long idFiscal) {
    return empresaRepository.findByIdFiscalAndEliminada(idFiscal, false);
  }

  private void validarOperacion(TipoDeOperacion operacion, Empresa empresa) {
    // Duplicados
    // Nombre
    Empresa empresaDuplicada = this.getEmpresaPorNombre(empresa.getNombre());
    if (operacion == TipoDeOperacion.ALTA && empresaDuplicada != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_empresa_duplicado_nombre", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION) {
      if (empresaDuplicada != null && empresaDuplicada.getIdEmpresa() != empresa.getIdEmpresa()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_empresa_duplicado_nombre", null, Locale.getDefault()));
      }
    }
    // ID Fiscal
    empresaDuplicada = this.getEmpresaPorIdFiscal(empresa.getIdFiscal());
    if (operacion == TipoDeOperacion.ALTA
        && empresaDuplicada != null
        && empresa.getIdFiscal() != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_empresa_duplicado_cuip", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && empresaDuplicada != null
        && empresaDuplicada.getIdEmpresa() != empresa.getIdEmpresa()
        && empresa.getIdFiscal() != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_empresa_duplicado_cuip", null, Locale.getDefault()));
    }
    if (empresa.getUbicacion() != null
      && empresa.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_sin_localidad", null, Locale.getDefault()));
    }
  }

  private void crearConfiguracionDelSistema(Empresa empresa) {
    ConfiguracionDelSistema cds = new ConfiguracionDelSistema();
    cds.setUsarFacturaVentaPreImpresa(false);
    cds.setCantidadMaximaDeRenglonesEnFactura(28);
    cds.setFacturaElectronicaHabilitada(false);
    cds.setEmpresa(empresa);
    configuracionDelSistemaService.guardar(cds);
  }

  @Override
  @Transactional
  public Empresa guardar(@Valid Empresa empresa) {
    if (empresa.getUbicacion() != null && empresa.getUbicacion().getIdLocalidad() != null) {
      empresa
          .getUbicacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(empresa.getUbicacion().getIdLocalidad()));
    }
    validarOperacion(TipoDeOperacion.ALTA, empresa);
    empresa = empresaRepository.save(empresa);
    crearConfiguracionDelSistema(empresa);
    logger.warn("La Empresa {} se guardó correctamente.", empresa);
    return empresa;
  }

  @Override
  @Transactional
  public void actualizar(@Valid Empresa empresaParaActualizar, Empresa empresaPersistida) {
    if (empresaPersistida.getLogo() != null
        && !empresaPersistida.getLogo().isEmpty()
        && (empresaParaActualizar.getLogo() == null || empresaParaActualizar.getLogo().isEmpty())) {
      photoVideoUploader.borrarImagen(
          Empresa.class.getSimpleName() + empresaPersistida.getIdEmpresa());
    }
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, empresaParaActualizar);
    empresaRepository.save(empresaParaActualizar);
  }

  @Override
  @Transactional
  public void eliminar(Long idEmpresa) {
    Empresa empresa = this.getEmpresaPorId(idEmpresa);
    empresa.setEliminada(true);
    empresa.setUbicacion(null);
    if (empresa.getLogo() != null && !empresa.getLogo().isEmpty()) {
      photoVideoUploader.borrarImagen(Empresa.class.getSimpleName() + empresa.getIdEmpresa());
    }
    configuracionDelSistemaService.eliminar(
        configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa));
    empresaRepository.save(empresa);
  }

  @Override
  public String guardarLogo(long idEmpresa, byte[] imagen) {
    if (imagen.length > 1024000L)
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_error_tamanio_no_valido", null, Locale.getDefault()));
    return photoVideoUploader.subirImagen(Empresa.class.getSimpleName() + idEmpresa, imagen);
  }
}
