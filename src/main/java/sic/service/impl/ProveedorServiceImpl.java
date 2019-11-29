package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import sic.modelo.criteria.BusquedaProveedorCriteria;
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
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
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
  public List<Proveedor> getProveedores() {
    return proveedorRepository.findAllByAndEliminadoOrderByRazonSocialAsc(false);
  }

  @Override
  public Page<Proveedor> buscarProveedores(BusquedaProveedorCriteria criteria) {
    QProveedor qProveedor = QProveedor.proveedor;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getNroProveedor() != null) {
      String[] terminos = criteria.getRazonSocial().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qProveedor.razonSocial.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getIdFiscal() != null) builder.or(qProveedor.idFiscal.eq(criteria.getIdFiscal()));
    if (criteria.getNroProveedor() != null)
      builder.or(qProveedor.nroProveedor.containsIgnoreCase(criteria.getNroProveedor()));
    if (criteria.getIdLocalidad() != null)
      builder.and(qProveedor.ubicacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.getIdProvincia() != null)
      builder.and(qProveedor.ubicacion.localidad.provincia.idProvincia.eq(criteria.getIdProvincia()));
    return proveedorRepository.findAll(builder, this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "razonSocial";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public Proveedor getProveedorPorRazonSocial(String razonSocial) {
    return proveedorRepository.findByRazonSocialAndEliminado(razonSocial, false);
  }

  private void validarOperacion(TipoDeOperacion operacion, Proveedor proveedor) {
    // Duplicados
    // ID Fiscal
    if (proveedor.getIdFiscal() != null) {
      List<Proveedor> proveedores =
          proveedorRepository.findByIdFiscalAndEliminado(proveedor.getIdFiscal(), false);
      if (proveedores.size() > 1
          || operacion.equals(TipoDeOperacion.ACTUALIZACION)
              && !proveedores.isEmpty()
              && proveedores.get(0).getIdProveedor() != proveedor.getIdProveedor()) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_proveedor_duplicado_idFiscal", null, Locale.getDefault()));
      }
      if (operacion.equals(TipoDeOperacion.ALTA)
          && !proveedores.isEmpty()
          && proveedor.getIdFiscal() != null) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_proveedor_duplicado_idFiscal", null, Locale.getDefault()));
      }
    }
    // Razon social
    Proveedor proveedorDuplicado = this.getProveedorPorRazonSocial(proveedor.getRazonSocial());
    if (operacion == TipoDeOperacion.ALTA && proveedorDuplicado != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_proveedor_duplicado_razonSocial", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && proveedorDuplicado != null
        && proveedorDuplicado.getIdProveedor() != proveedor.getIdProveedor()) {
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
    proveedor.setNroProveedor(this.generarNroDeProveedor());
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
    cuentaCorrienteProveedor.setSaldo(BigDecimal.ZERO);
    cuentaCorrienteProveedor.setFechaApertura(LocalDateTime.now());
    cuentaCorrienteProveedor.setFechaUltimoMovimiento(LocalDateTime.now());
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
  public String generarNroDeProveedor() {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroProveedor = Long.toString(randomLong);
      Proveedor p =
          proveedorRepository.findByNroProveedorAndEliminado(nroProveedor, false);
      if (p == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
