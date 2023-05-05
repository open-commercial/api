package sic.service.impl;

import java.util.Arrays;
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
import sic.modelo.Ubicacion;
import sic.modelo.dto.NuevaSucursalDTO;
import sic.service.*;
import sic.modelo.TipoDeOperacion;
import sic.repository.SucursalRepository;
import sic.exception.BusinessServiceException;
import sic.util.CustomValidator;

@Service
public class SucursalServiceImpl implements ISucursalService {

  private final SucursalRepository sucursalRepository;
  private final IConfiguracionSucursalService configuracionSucursalService;
  private final IPhotoUploader photoUploader;
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
    IPhotoUploader photoUploader,
    IProductoService productoService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.sucursalRepository = sucursalRepository;
    this.configuracionSucursalService = configuracionSucursalService;
    this.ubicacionService = ubicacionService;
    this.photoUploader = photoUploader;
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
    /* Momentaneamente se anula la validación por ID fiscal duplicado
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
    }*/
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
  public Sucursal guardar(NuevaSucursalDTO nuevaSucursal, Ubicacion ubicacion, byte[] logo) {
    customValidator.validar(nuevaSucursal);
    Sucursal sucursalParaAlta = new Sucursal();
    sucursalParaAlta.setNombre(nuevaSucursal.getNombre());
    sucursalParaAlta.setLema(nuevaSucursal.getLema());
    sucursalParaAlta.setCategoriaIVA(nuevaSucursal.getCategoriaIVA());
    sucursalParaAlta.setIdFiscal(nuevaSucursal.getIdFiscal());
    sucursalParaAlta.setIngresosBrutos(nuevaSucursal.getIngresosBrutos());
    sucursalParaAlta.setFechaInicioActividad(nuevaSucursal.getFechaInicioActividad());
    sucursalParaAlta.setEmail(nuevaSucursal.getEmail());
    sucursalParaAlta.setTelefono(nuevaSucursal.getTelefono());
    sucursalParaAlta.setUbicacion(ubicacion);
    if (sucursalParaAlta.getUbicacion() != null && sucursalParaAlta.getUbicacion().getIdLocalidad() != null) {
      sucursalParaAlta
              .getUbicacion()
              .setLocalidad(
                      ubicacionService.getLocalidadPorId(sucursalParaAlta.getUbicacion().getIdLocalidad()));
    }
    this.crearConfiguracionSucursal(sucursalParaAlta);
    validarReglasDeNegocio(TipoDeOperacion.ALTA, sucursalParaAlta);
    sucursalParaAlta = sucursalRepository.save(sucursalParaAlta);
    this.productoService.guardarCantidadesDeSucursalNueva(sucursalParaAlta);
    logger.warn("La Sucursal {} se guardó correctamente.", nuevaSucursal);
    if (logo != null)
      sucursalParaAlta.setLogo(
              this.guardarLogo(sucursalParaAlta.getIdSucursal(), logo));
    return sucursalParaAlta;
  }

  @Override
  @Transactional
  public void actualizar(Sucursal sucursalParaActualizar, Sucursal sucursalPersistida, byte[] imagen) {
    customValidator.validar(sucursalParaActualizar);
    if (imagen != null) {
      if (imagen.length == 0) {
        if (sucursalPersistida.getLogo() != null
                && !sucursalPersistida.getLogo().isEmpty()) {
          photoUploader.borrarImagen(
                  Sucursal.class.getSimpleName() + sucursalPersistida.getIdSucursal());
          sucursalParaActualizar.setLogo(null);
        }
      } else {
        sucursalParaActualizar.setLogo(this.guardarLogo(sucursalPersistida.getIdSucursal(), imagen));
      }
    }
    sucursalParaActualizar.setConfiguracionSucursal(sucursalPersistida.getConfiguracionSucursal());
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, sucursalParaActualizar);
    photoUploader.isUrlValida(sucursalParaActualizar.getLogo());
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
    if (sucursal.getLogo() != null && !sucursal.getLogo().isEmpty()) {
      photoUploader.borrarImagen(Sucursal.class.getSimpleName() + sucursal.getIdSucursal());
    }
    productoService.eliminarCantidadesDeSucursal(sucursal);
    sucursalRepository.save(sucursal);
  }

  @Override
  public String guardarLogo(long idSucursal, byte[] imagen) {
    if (imagen.length > 1024000L)
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_error_tamanio_no_valido", null, Locale.getDefault()));
    return photoUploader.subirImagen(Sucursal.class.getSimpleName() + idSucursal, imagen);
  }
}
