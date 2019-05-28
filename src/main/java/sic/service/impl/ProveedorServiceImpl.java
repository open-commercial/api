package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.service.IProveedorService;
import sic.service.BusinessServiceException;
import sic.repository.ProveedorRepository;
import sic.service.ICuentaCorrienteService;
import sic.service.IUbicacionService;

@Service
@Validated
public class ProveedorServiceImpl implements IProveedorService {

  private final ProveedorRepository proveedorRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IUbicacionService ubicacionService;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public ProveedorServiceImpl(
      ProveedorRepository proveedorRepository,
      ICuentaCorrienteService cuentaCorrienteService,
      IUbicacionService ubicacionService) {
    this.proveedorRepository = proveedorRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.ubicacionService = ubicacionService;
  }

  @Override
  public Proveedor getProveedorNoEliminadoPorId(long idProveedor) {
    Optional<Proveedor> proveedor = proveedorRepository
      .findById(idProveedor);
    if (proveedor.isPresent() && !proveedor.get().isEliminado()) {
      return proveedor.get();
    } else {
      throw new EntityNotFoundException(
        ResourceBundle.getBundle("Mensajes")
          .getString("mensaje_proveedor_no_existente"));
    }
  }

  @Override
  public List<Proveedor> getProveedores(Empresa empresa) {
    return proveedorRepository.findAllByAndEmpresaAndEliminadoOrderByRazonSocialAsc(empresa, false);
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
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qProveedor.eliminado.eq(false)));
    Page<Proveedor> proveedores = proveedorRepository.findAll(builder, criteria.getPageable());
    return proveedores;
  }

  @Override
  public Proveedor getProveedorPorIdFiscal(Long idFiscal, Empresa empresa) {
    return proveedorRepository.findByIdFiscalAndEmpresaAndEliminado(idFiscal, empresa, false);
  }

  @Override
  public Proveedor getProveedorPorRazonSocial(String razonSocial, Empresa empresa) {
    return proveedorRepository.findByRazonSocialAndEmpresaAndEliminado(razonSocial, empresa, false);
  }

  private void validarOperacion(TipoDeOperacion operacion, Proveedor proveedor) {
    // Duplicados
    // ID Fiscal
    if (proveedor.getIdFiscal() != null) {
      Proveedor proveedorDuplicado =
          this.getProveedorPorIdFiscal(proveedor.getIdFiscal(), proveedor.getEmpresa());
      if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
          && proveedorDuplicado != null
          && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_proveedor_duplicado_idFiscal"));
      }
      if (operacion.equals(TipoDeOperacion.ALTA)
          && proveedorDuplicado != null
          && proveedor.getIdFiscal() != null) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_proveedor_duplicado_idFiscal"));
      }
    }
    // Razon social
    Proveedor proveedorDuplicado =
        this.getProveedorPorRazonSocial(proveedor.getRazonSocial(), proveedor.getEmpresa());
    if (operacion == TipoDeOperacion.ALTA && proveedorDuplicado != null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_proveedor_duplicado_razonSocial"));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && proveedorDuplicado != null
        && proveedorDuplicado.getId_Proveedor() != proveedor.getId_Proveedor()) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_proveedor_duplicado_razonSocial"));
    }
    // Ubicacion
    if (proveedor.getUbicacion() != null && proveedor.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_ubicacion_sin_localidad"));
    }
  }

  @Override
  @Transactional
  public Proveedor guardar(@Validated Proveedor proveedor) {
    proveedor.setNroProveedor(this.generarNroDeProveedor(proveedor.getEmpresa()));
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
    cuentaCorrienteProveedor.setEmpresa(proveedor.getEmpresa());
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
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_proveedor_no_existente"));
    }
    cuentaCorrienteService.eliminarCuentaCorrienteProveedor(idProveedor);
    proveedor.setEliminado(true);
    proveedor.setUbicacion(null);
    proveedorRepository.save(proveedor);
  }

  @Override
  public String generarNroDeProveedor(Empresa empresa) {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroProveedor = Long.toString(randomLong);
      Proveedor p =
          proveedorRepository.findByNroProveedorAndEmpresaAndEliminado(
              nroProveedor, empresa, false);
      if (p == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }
}
