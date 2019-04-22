package sic.service.impl;

import java.util.List;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Empresa;
import sic.service.*;
import sic.modelo.TipoDeOperacion;
import sic.util.Validator;
import sic.repository.EmpresaRepository;

@Service
public class EmpresaServiceImpl implements IEmpresaService {

  private final EmpresaRepository empresaRepository;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final IPhotoVideoUploader photoVideoUploader;
  private final IUbicacionService ubicacionService;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public EmpresaServiceImpl(
      EmpresaRepository empresaRepository,
      IConfiguracionDelSistemaService configuracionDelSistemaService,
      IUbicacionService ubicacionService,
      IPhotoVideoUploader photoVideoUploader) {
    this.empresaRepository = empresaRepository;
    this.configuracionDelSistemaService = configuracionDelSistemaService;
    this.ubicacionService = ubicacionService;
    this.photoVideoUploader = photoVideoUploader;
  }

  @Override
  public Empresa getEmpresaPorId(Long idEmpresa) {
    Empresa empresa = empresaRepository.findById(idEmpresa);
    if (empresa == null) {
      throw new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_empresa_no_existente"));
    }
    return empresa;
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
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_empresa_duplicado_nombre"));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION) {
      if (empresaDuplicada != null && empresaDuplicada.getId_Empresa() != empresa.getId_Empresa()) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_empresa_duplicado_nombre"));
      }
    }
    // ID Fiscal
    empresaDuplicada = this.getEmpresaPorIdFiscal(empresa.getIdFiscal());
    if (operacion == TipoDeOperacion.ALTA
        && empresaDuplicada != null
        && empresa.getIdFiscal() != null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_empresa_duplicado_cuip"));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && empresaDuplicada != null
        && empresaDuplicada.getId_Empresa() != empresa.getId_Empresa()
        && empresa.getIdFiscal() != null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_empresa_duplicado_cuip"));
    }
    if (empresa.getUbicacion() != null
      && empresa.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_ubicacion_sin_localidad"));
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
    LOGGER.warn("La Empresa {} se guardó correctamente.", empresa);
    return empresa;
  }

  @Override
  @Transactional
  public void actualizar(Empresa empresaParaActualizar, Empresa empresaPersistida) {
    if (empresaPersistida.getLogo() != null
        && !empresaPersistida.getLogo().isEmpty()
        && (empresaParaActualizar.getLogo() == null || empresaParaActualizar.getLogo().isEmpty())) {
      photoVideoUploader.borrarImagen(
          Empresa.class.getSimpleName() + empresaPersistida.getId_Empresa());
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
      photoVideoUploader.borrarImagen(Empresa.class.getSimpleName() + empresa.getId_Empresa());
    }
    configuracionDelSistemaService.eliminar(
        configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresa));
    empresaRepository.save(empresa);
  }

  @Override
  public String guardarLogo(long idEmpresa, byte[] imagen) {
    if (imagen.length > 1024000L)
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_error_tamanio_no_valido"));
    return photoVideoUploader.subirImagen(Empresa.class.getSimpleName() + idEmpresa, imagen);
  }
}
