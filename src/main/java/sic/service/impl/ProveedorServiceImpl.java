package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.service.IProveedorService;
import sic.exception.BusinessServiceException;
import sic.repository.ProveedorRepository;
import sic.service.ICuentaCorrienteService;
import sic.service.IUbicacionService;

@Service
@Validated
public class ProveedorServiceImpl implements IProveedorService {

  private final ProveedorRepository proveedorRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IUbicacionService ubicacionService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  public ProveedorServiceImpl(
      ProveedorRepository proveedorRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IUbicacionService ubicacionService,
      MessageSource messageSource) {
    this.proveedorRepository = proveedorRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.ubicacionService = ubicacionService;
    this.messageSource = messageSource;
  }

  @Override
  public Proveedor getProveedorNoEliminadoPorId(long idProveedor) {
    Optional<Proveedor> proveedor = proveedorRepository
      .findById(idProveedor);
    if (proveedor.isPresent() && !proveedor.get().isEliminado()) {
      return proveedor.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_proveedor_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public List<Proveedor> getProveedores(Sucursal sucursal) {
    return proveedorRepository.findAllByAndSucursalAndEliminadoOrderByRazonSocialAsc(sucursal, false);
  }

  @Override
  public Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria) {
    QProveedor qProveedor = QProveedor.proveedor;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorRazonSocial()) {
      String[] terminos = criteria.getRazonSocial().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qProveedor.razonSocial.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorIdFiscal()) builder.or(qProveedor.idFiscal.eq(criteria.getIdFiscal()));
    if (criteria.isBuscaPorNroProveedor())
      builder.or(qProveedor.nroProveedor.containsIgnoreCase(criteria.getNroProveedor()));
    if (criteria.isBuscaPorLocalidad())
      builder.and(qProveedor.ubicacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(qProveedor.ubicacion.localidad.provincia.idProvincia.eq(criteria.getIdProvincia()));
    builder.and(
        qProveedor
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qProveedor.eliminado.eq(false)));
    return proveedorRepository.findAll(builder, criteria.getPageable());
  }

  @Override
  public Proveedor getProveedorPorIdFiscal(Long idFiscal, Sucursal sucursal) {
    return proveedorRepository.findByIdFiscalAndSucursalAndEliminado(idFiscal, sucursal, false);
  }

  @Override
  public Proveedor getProveedorPorRazonSocial(String razonSocial, Sucursal sucursal) {
    return proveedorRepository.findByRazonSocialAndSucursalAndEliminado(razonSocial, sucursal, false);
  }

  private void validarOperacion(TipoDeOperacion operacion, Proveedor proveedor) {
    // Duplicados
    // ID Fiscal
    if (proveedor.getIdFiscal() != null) {
      Proveedor proveedorDuplicado =
          this.getProveedorPorIdFiscal(proveedor.getIdFiscal(), proveedor.getSucursal());
      if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
          && proveedorDuplicado != null
          && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_proveedor_duplicado_idFiscal", null, Locale.getDefault()));
      }
      if (operacion.equals(TipoDeOperacion.ALTA)
          && proveedorDuplicado != null
          && proveedor.getIdFiscal() != null) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_proveedor_duplicado_idFiscal", null, Locale.getDefault()));
      }
    }
    // Razon social
    Proveedor proveedorDuplicado =
        this.getProveedorPorRazonSocial(proveedor.getRazonSocial(), proveedor.getSucursal());
    if (operacion == TipoDeOperacion.ALTA && proveedorDuplicado != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_proveedor_duplicado_razonSocial", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && proveedorDuplicado != null
        && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_proveedor_duplicado_razonSocial", null, Locale.getDefault()));
    }
    // Ubicacion
    if (proveedor.getUbicacion() != null && proveedor.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_sin_localidad", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public Proveedor guardar(@Validated Proveedor proveedor) {
    proveedor.setNroProveedor(this.generarNroDeProveedor(proveedor.getSucursal()));
    if (proveedor.getUbicacion() != null && proveedor.getUbicacion().getIdLocalidad() != null) {
      proveedor
          .getUbicacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(proveedor.getUbicacion().getIdLocalidad()));
    }
    this.validarOperacion(TipoDeOperacion.ALTA, proveedor);
    proveedor = proveedorRepository.save(proveedor);
    CuentaCorrienteProveedor cuentaCorrienteProveedor = new CuentaCorrienteProveedor();
    cuentaCorrienteProveedor.setProveedor(proveedor);
    cuentaCorrienteProveedor.setSucursal(proveedor.getSucursal());
    cuentaCorrienteProveedor.setSaldo(BigDecimal.ZERO);
    cuentaCorrienteProveedor.setFechaApertura(new Date());
    cuentaCorrienteService.guardarCuentaCorrienteProveedor(cuentaCorrienteProveedor);
    logger.warn("El Proveedor {} se guardó correctamente.", proveedor);
    return proveedor;
  }

  @Override
  @Transactional
  public void actualizar(@Valid Proveedor proveedor) {
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, proveedor);
    proveedorRepository.save(proveedor);
  }

  @Override
  @Transactional
  public void eliminar(long idProveedor) {
    Proveedor proveedor = this.getProveedorNoEliminadoPorId(idProveedor);
    if (proveedor == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_proveedor_no_existente", null, Locale.getDefault()));
    }
    cuentaCorrienteService.eliminarCuentaCorrienteProveedor(idProveedor);
    proveedor.setEliminado(true);
    proveedor.setUbicacion(null);
    proveedorRepository.save(proveedor);
  }

  @Override
  public String generarNroDeProveedor(Sucursal sucursal) {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroProveedor = Long.toString(randomLong);
      Proveedor p =
          proveedorRepository.findByNroProveedorAndSucursalAndEliminado(
              nroProveedor, sucursal, false);
      if (p == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
