package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.repository.LocalidadRepository;
import sic.repository.ProvinciaRepository;
import sic.repository.UbicacionRepository;
import sic.service.*;
import sic.util.Validator;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.ResourceBundle;

@Service
public class UbicacionServiceImpl implements IUbicacionService {

  private final UbicacionRepository ubicacionRepository;
  private final LocalidadRepository localidadRepository;
  private final ProvinciaRepository provinciaRepository;
  private final IClienteService clienteService;
  private final IEmpresaService empresaService;
  private final IProveedorService proveedorService;
  private final ITransportistaService transportistaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public UbicacionServiceImpl(
    UbicacionRepository ubicacionRepository,
    LocalidadRepository localidadRepository,
    ProvinciaRepository provinciaRepository,
    IClienteService clienteService,
    IEmpresaService empresaService,
    IProveedorService proveedorService,
    ITransportistaService transportistaService) {
    this.ubicacionRepository = ubicacionRepository;
    this.localidadRepository = localidadRepository;
    this.provinciaRepository = provinciaRepository;
    this.empresaService = empresaService;
    this.proveedorService = proveedorService;
    this.transportistaService = transportistaService;
    this.clienteService = clienteService;
  }

  @Override
  @Transactional
  public Ubicacion getUbicacionPorId(long idUbicacion) {
    return ubicacionRepository.findById(idUbicacion).orElse(null);
  }

  @Override
  @Transactional
  public Ubicacion guardar(
    Ubicacion ubicacion) {
    this.validarUbicacion(ubicacion);
    Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
    logger.warn("La ubicación {} se actualizó correctamente.", ubicacion);
    return ubicacionGuardada;
  }

  @Override
  @Transactional
  public Ubicacion guardarUbicacionDeFacturacionCliente(
    Ubicacion ubicacion,
    Cliente cliente) {
    if (cliente.getUbicacionFacturacion() != null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_error_ubicacion_incorrecta_cliente"));
    }
    cliente.setUbicacionFacturacion(
      this.guardar(ubicacion));
    clienteService.actualizar(cliente, clienteService.getClienteNoEliminadoPorId(cliente.getId_Cliente()));
    return cliente.getUbicacionFacturacion();
  }

  @Override
  @Transactional
  public Ubicacion guardarUbicacionDeEnvioCliente(
    Ubicacion ubicacion,
    Cliente cliente) {
    if (cliente.getUbicacionEnvio() != null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_error_ubicacion_incorrecta_cliente"));
    }
    cliente.setUbicacionEnvio(
      this.guardar(ubicacion));
    clienteService.actualizar(cliente, clienteService.getClienteNoEliminadoPorId(cliente.getId_Cliente()));
    return cliente.getUbicacionEnvio();
  }

  @Override
  @Transactional
  public Ubicacion guardaUbicacionEmpresa(
    Ubicacion ubicacion,
    Empresa empresa) {
    if (empresa.getUbicacion() != null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_error_ubicacion_incorrecta_empresa"));
    }
    empresa.setUbicacion(this.guardar(ubicacion));
    empresaService.actualizar(empresa, empresaService.getEmpresaPorId(empresa.getId_Empresa()));
    return empresa.getUbicacion();
  }

  @Override
  @Transactional
  public Ubicacion guardaUbicacionProveedor(
    Ubicacion ubicacion,
    Proveedor proveedor) {
    if (proveedor.getUbicacion() != null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_error_ubicacion_incorrecta_proveedor"));
    }
    proveedor.setUbicacion(this.guardar(ubicacion));
    proveedorService.actualizar(proveedor);
    return proveedor.getUbicacion();
  }

  @Override
  @Transactional
  public Ubicacion guardarUbicacionTransportista(
    Ubicacion ubicacion,
    Transportista transportista) {
    if (transportista.getUbicacion() != null) {
      throw new BusinessServiceException(
        RESOURCE_BUNDLE.getString("mensaje_error_ubicacion_incorrecta_transportista"));
    }
    transportista.setUbicacion(
      this.guardar(ubicacion));
    transportistaService.actualizar(transportista);
    return transportista.getUbicacion();
  }

  @Override
  public void actualizar(
    Ubicacion ubicacion) {
    this.validarUbicacion(ubicacion);
    ubicacionRepository.save(ubicacion);
  }

  private void validarUbicacion(
    Ubicacion ubicacion) {
    if (ubicacion.getLocalidad() != null) {
      this.validarOperacion(TipoDeOperacion.ACTUALIZACION, ubicacion.getLocalidad());
    }
  }

  @Override
  public Localidad getLocalidadPorId(Long idLocalidad) {
    return localidadRepository
        .findById(idLocalidad)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    RESOURCE_BUNDLE.getString("mensaje_localidad_no_existente")));
  }

  @Override
  public Localidad getLocalidadPorNombre(String nombre, Provincia provincia) {
    return localidadRepository.findByNombreAndProvinciaOrderByNombreAsc(nombre, provincia);
  }

  @Override
  public List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia) {
    return localidadRepository.findAllByAndProvinciaOrderByNombreAsc(provincia);
  }

  @Override
  public Provincia getProvinciaPorId(Long idProvincia) {
    return provinciaRepository
        .findById(idProvincia)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_provincia_no_existente")));
  }

  @Override
  public Provincia getProvinciaPorNombre(String nombre) {
    return provinciaRepository.findByNombreOrderByNombreAsc(nombre);
  }

  @Override
  public List<Provincia> getProvincias() {
    return provinciaRepository.findAllByOrderByNombreAsc();
  }

  @Override
  @Transactional
  public void actualizarLocalidad(Localidad localidad) {
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, localidad);
    localidadRepository.save(localidad);
  }

  @Override
  public void validarOperacion(TipoDeOperacion operacion, Localidad localidad) {
    //Requeridos
    if (Validator.esVacio(localidad.getNombre())) {
      throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
        .getString("mensaje_localidad_vacio_nombre"));
    }
    if (Validator.esVacio(localidad.getCodigoPostal())) {
      throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
        .getString("mensaje_localidad_codigo_postal_vacio"));
    }
    if (localidad.getProvincia() == null) {
      throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
        .getString("mensaje_localidad_provincia_vacio"));
    }
    //Duplicados
    //Nombre
    Localidad localidadDuplicada = this.getLocalidadPorNombre(localidad.getNombre(), localidad.getProvincia());
    if (operacion.equals(TipoDeOperacion.ALTA) && localidadDuplicada != null) {
      throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
        .getString("mensaje_localidad_duplicado_nombre"));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)) {
      if (localidadDuplicada != null && localidadDuplicada.getIdLocalidad() != localidad.getIdLocalidad()) {
        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
          .getString("mensaje_localidad_duplicado_nombre"));
      }
    }
  }

  @Override
  public Page<Localidad> buscar(BusquedaLocalidadCriteria criteria) {
    QLocalidad qLocalidad = QLocalidad.localidad;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorNombre()) {
      String[] terminos = criteria.getNombre().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qLocalidad.nombre.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorCodigoPostal()) {
      String[] terminos = criteria.getCodigoPostal().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qLocalidad.codigoPostal.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorNombreProvincia()) {
      String[] terminos = criteria.getNombreProvincia().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qLocalidad.provincia.nombre.containsIgnoreCase(termino));
      }
      builder.and(rsPredicate);
    }
    if (criteria.isBuscaPorEnvio()) {
      builder.and(qLocalidad.envioGratuito.eq(criteria.getEnvioGratuito()));
    }
    return localidadRepository.findAll(builder, criteria.getPageable());
  }
}
