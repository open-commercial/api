package org.opencommercial.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.CuentaCorrienteProveedor;
import org.opencommercial.model.Proveedor;
import org.opencommercial.model.QProveedor;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.criteria.BusquedaProveedorCriteria;
import org.opencommercial.repository.ProveedorRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class ProveedorServiceImpl implements ProveedorService {

  private final ProveedorRepository proveedorRepository;
  private final CuentaCorrienteService cuentaCorrienteService;
  private final UbicacionService ubicacionService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public ProveedorServiceImpl(
    ProveedorRepository proveedorRepository,
    CuentaCorrienteService cuentaCorrienteService,
    UbicacionService ubicacionService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.proveedorRepository = proveedorRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.ubicacionService = ubicacionService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public Proveedor getProveedorNoEliminadoPorId(long idProveedor) {
    Optional<Proveedor> proveedor = proveedorRepository.findById(idProveedor);
    if (proveedor.isPresent() && !proveedor.get().isEliminado()) {
      return proveedor.get();
    } else {
      throw new EntityNotFoundException(
              messageSource.getMessage("mensaje_proveedor_no_existente", null, Locale.getDefault()));
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
    if (criteria.getIdFiscal() != null && criteria.getIdFiscal().matches("\\d+"))
      builder.or(qProveedor.idFiscal.eq(Long.valueOf(criteria.getIdFiscal())));
    if (criteria.getNroProveedor() != null)
      builder.or(qProveedor.nroProveedor.containsIgnoreCase(criteria.getNroProveedor()));
    if (criteria.getIdLocalidad() != null)
      builder.and(qProveedor.ubicacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.getIdProvincia() != null)
      builder.and(qProveedor.ubicacion.localidad.provincia.idProvincia.eq(criteria.getIdProvincia()));
    builder.and(qProveedor.eliminado.eq(false));
    return proveedorRepository.findAll(builder, this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "razonSocial";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public Proveedor getProveedorPorRazonSocial(String razonSocial) {
    return proveedorRepository.findByRazonSocialAndEliminado(razonSocial, false);
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Proveedor proveedor) {
    // Duplicados
    // ID Fiscal
    if (proveedor.getIdFiscal() != null) {
      var proveedores = proveedorRepository.findByIdFiscalAndEliminado(proveedor.getIdFiscal(), false);
      if (proveedores.size() > 1
          || operacion.equals(TipoDeOperacion.ACTUALIZACION)
              && !proveedores.isEmpty()
              && proveedores.get(0).getIdProveedor() != proveedor.getIdProveedor()) {
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_proveedor_duplicado_idFiscal", null, Locale.getDefault()));
      }
      if (operacion.equals(TipoDeOperacion.ALTA)
          && !proveedores.isEmpty()
          && proveedor.getIdFiscal() != null) {
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_proveedor_duplicado_idFiscal", null, Locale.getDefault()));
      }
    }
    // Razon social
    Proveedor proveedorDuplicado = this.getProveedorPorRazonSocial(proveedor.getRazonSocial());
    if (operacion == TipoDeOperacion.ALTA && proveedorDuplicado != null) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_proveedor_duplicado_razonSocial", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && proveedorDuplicado != null
        && proveedorDuplicado.getIdProveedor() != proveedor.getIdProveedor()) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_proveedor_duplicado_razonSocial", null, Locale.getDefault()));
    }
    // Ubicacion
    if (proveedor.getUbicacion() != null && proveedor.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_ubicacion_sin_localidad", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public Proveedor guardar(Proveedor proveedor) {
    customValidator.validar(proveedor);
    proveedor.setNroProveedor(this.generarNroDeProveedor());
    if (proveedor.getUbicacion() != null && proveedor.getUbicacion().getIdLocalidad() != null) {
      proveedor
          .getUbicacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(proveedor.getUbicacion().getIdLocalidad()));
    }
    this.validarReglasDeNegocio(TipoDeOperacion.ALTA, proveedor);
    proveedor = proveedorRepository.save(proveedor);
    CuentaCorrienteProveedor cuentaCorrienteProveedor = new CuentaCorrienteProveedor();
    cuentaCorrienteProveedor.setProveedor(proveedor);
    cuentaCorrienteProveedor.setSaldo(BigDecimal.ZERO);
    cuentaCorrienteProveedor.setFechaApertura(LocalDateTime.now());
    cuentaCorrienteProveedor.setFechaUltimoMovimiento(LocalDateTime.now());
    cuentaCorrienteService.guardarCuentaCorrienteProveedor(cuentaCorrienteProveedor);
    log.info("El Proveedor {} se guardó correctamente.", proveedor);
    return proveedor;
  }

  @Override
  @Transactional
  public void actualizar(Proveedor proveedor) {
    customValidator.validar(proveedor);
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, proveedor);
    proveedorRepository.save(proveedor);
  }

  @Override
  @Transactional
  public void eliminar(long idProveedor) {
    Proveedor proveedor = this.getProveedorNoEliminadoPorId(idProveedor);
    if (proveedor == null) {
      throw new EntityNotFoundException(
              messageSource.getMessage("mensaje_proveedor_no_existente", null, Locale.getDefault()));
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
      Proveedor p = proveedorRepository.findByNroProveedorAndEliminado(nroProveedor, false);
      if (p == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
