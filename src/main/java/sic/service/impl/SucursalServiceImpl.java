package sic.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Sucursal;
import sic.service.*;
import sic.modelo.TipoDeOperacion;
import sic.repository.SucursalRepository;
import sic.exception.BusinessServiceException;
import sic.util.CustomValidator;

@Service
public class SucursalServiceImpl implements ISucursalService {

  private final SucursalRepository sucursalRepository;
  private final IConfiguracionSucursalService configuracionSucursalService;
  private final IPhotoVideoUploader photoVideoUploader;
  private final IUbicacionService ubicacionService;
  private final IProductoService productoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public SucursalServiceImpl(
    SucursalRepository sucursalRepository,
    IConfiguracionSucursalService configuracionSucursalService,
    IUbicacionService ubicacionService,
    IPhotoVideoUploader photoVideoUploader,
    IProductoService productoService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.sucursalRepository = sucursalRepository;
    this.configuracionSucursalService = configuracionSucursalService;
    this.ubicacionService = ubicacionService;
    this.photoVideoUploader = photoVideoUploader;
    this.productoService = productoService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public Sucursal getSucursalPorId(Long idSucursal) {
    Optional<Sucursal> sucursal = sucursalRepository.findById(idSucursal);
    if (sucursal.isPresent() && !sucursal.get().isEliminada()) {
      return sucursal.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_sucursal_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Sucursal getSucursalPredeterminada() {
    return sucursalRepository.getSucursalPredeterminada();
  }

  @Override
  public List<Sucursal> getSucusales(boolean puntoDeRetiro) {
    if (puntoDeRetiro) {
      return sucursalRepository.findAllByAndEliminadaOrderByNombreAsc(false).stream()
          .filter(
              sucursal ->
                  sucursal.getConfiguracionSucursal()
                      .isPuntoDeRetiro())
          .collect(Collectors.toList());
    } else {
      return sucursalRepository.findAllByAndEliminadaOrderByNombreAsc(false);
    }
  }

  @Override
  public Sucursal getSucursalPorNombre(String nombre) {
    return sucursalRepository.findByNombreIsAndEliminadaOrderByNombreAsc(nombre, false);
  }

  @Override
  public Sucursal getSucursalPorIdFiscal(Long idFiscal) {
    return sucursalRepository.findByIdFiscalAndEliminada(idFiscal, false);
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Sucursal sucursal) {
    // Duplicados
    // Nombre
    Sucursal sucursalDuplicada = this.getSucursalPorNombre(sucursal.getNombre());
    if (operacion == TipoDeOperacion.ALTA && sucursalDuplicada != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_sucursal_duplicado_nombre", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && sucursalDuplicada != null
        && sucursalDuplicada.getIdSucursal() != sucursal.getIdSucursal()) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_sucursal_duplicado_nombre", null, Locale.getDefault()));
    }
    // ID Fiscal
    sucursalDuplicada = this.getSucursalPorIdFiscal(sucursal.getIdFiscal());
    if (operacion == TipoDeOperacion.ALTA
        && sucursalDuplicada != null
        && sucursal.getIdFiscal() != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_sucursal_duplicado_cuip", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && sucursalDuplicada != null
        && sucursalDuplicada.getIdSucursal() != sucursal.getIdSucursal()
        && sucursal.getIdFiscal() != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_sucursal_duplicado_cuip", null, Locale.getDefault()));
    }
    if (sucursal.getUbicacion() != null
      && sucursal.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_sin_localidad", null, Locale.getDefault()));
    }
    if (sucursal.getConfiguracionSucursal() == null) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_sucursal_sin_configuracion", null, Locale.getDefault()));
    }
  }

  private void crearConfiguracionSucursal(Sucursal sucursal) {
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setUsarFacturaVentaPreImpresa(false);
    configuracionSucursal.setCantidadMaximaDeRenglonesEnFactura(28);
    configuracionSucursal.setFacturaElectronicaHabilitada(false);
    configuracionSucursal.setVencimientoCorto(1);
    configuracionSucursal.setVencimientoLargo(1);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
  }

  @Override
  @Transactional
  public Sucursal guardar(Sucursal sucursal) {
    customValidator.validar(sucursal);
    if (sucursal.getUbicacion() != null && sucursal.getUbicacion().getIdLocalidad() != null) {
      sucursal
          .getUbicacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(sucursal.getUbicacion().getIdLocalidad()));
    }
    this.crearConfiguracionSucursal(sucursal);
    validarReglasDeNegocio(TipoDeOperacion.ALTA, sucursal);
    sucursal = sucursalRepository.save(sucursal);
    this.productoService.guardarCantidadesDeSucursalNueva(sucursal);
    logger.warn("La Sucursal {} se guardó correctamente.", sucursal);
    return sucursal;
  }

  @Override
  @Transactional
  public void actualizar(Sucursal sucursalParaActualizar, Sucursal sucursalPersistida) {
    customValidator.validar(sucursalParaActualizar);
    if (sucursalPersistida.getLogo() != null
        && !sucursalPersistida.getLogo().isEmpty()
        && (sucursalParaActualizar.getLogo() == null || sucursalParaActualizar.getLogo().isEmpty())) {
      photoVideoUploader.borrarImagen(
          Sucursal.class.getSimpleName() + sucursalPersistida.getIdSucursal());
    }
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, sucursalParaActualizar);
    sucursalRepository.save(sucursalParaActualizar);
  }

  @Override
  @Transactional
  public void eliminar(Long idSucursal) {
    Sucursal sucursal = this.getSucursalPorId(idSucursal);
    if (sucursal.getConfiguracionSucursal().isPredeterminada()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_sucursal_no_se_puede_eliminar_predeterminada", null, Locale.getDefault()));
    }
    sucursal.setEliminada(true);
    sucursal.setUbicacion(null);
    if (sucursal.getLogo() != null && !sucursal.getLogo().isEmpty()) {
      photoVideoUploader.borrarImagen(Sucursal.class.getSimpleName() + sucursal.getIdSucursal());
    }
    configuracionSucursalService.eliminar(sucursal.getConfiguracionSucursal());
    sucursalRepository.save(sucursal);
  }

  @Override
  public String guardarLogo(long idSucursal, byte[] imagen) {
    if (imagen.length > 1024000L)
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_error_tamanio_no_valido", null, Locale.getDefault()));
    return photoVideoUploader.subirImagen(Sucursal.class.getSimpleName() + idSucursal, imagen);
  }
}
