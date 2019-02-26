package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.repository.LocalidadRepository;
import sic.repository.ProvinciaRepository;
import sic.repository.UbicacionRepository;
import sic.service.BusinessServiceException;
import sic.service.IUbicacionService;
import sic.util.Validator;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.ResourceBundle;

@Service
public class UbicacionServiceImpl implements IUbicacionService {

  private final UbicacionRepository ubicacionRepository;
  private final LocalidadRepository localidadRepository;
  private final ProvinciaRepository provinciaRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public UbicacionServiceImpl(
    UbicacionRepository ubicacionRepository,
    LocalidadRepository localidadRepository,
    ProvinciaRepository provinciaRepository) {
    this.ubicacionRepository = ubicacionRepository;
    this.localidadRepository = localidadRepository;
    this.provinciaRepository = provinciaRepository;
  }

  @Override
  public Ubicacion guardar(Ubicacion ubicacion) {
    this.validarUbicacion(ubicacion);
    Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
    logger.warn("La ubicación {} se actualizó correctamente.", ubicacion);
    return ubicacionGuardada;
  }

  private void validarUbicacion(Ubicacion ubicacion) {
    if (ubicacion.getLocalidad() != null)
      ubicacion.setLocalidad(this.guardarLocalidad(
        ubicacion.getLocalidad().getNombre(),
        ubicacion.getLocalidad().getNombreProvincia(),
        ubicacion.getLocalidad().getCodigoPostal()));
  }

  @Override
  public Localidad guardarLocalidad(String nombre, String nombreProvincia, String codigoPostal) {
    if (codigoPostal == null || codigoPostal.isEmpty())
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_ubicacion_codigo_postal_vacio"));
    Provincia provincia = this.getProvinciaPorNombre(nombreProvincia);
    if (provincia == null) {
      provincia = new Provincia();
      provincia.setNombre(nombreProvincia);
      provincia = provinciaRepository.save(provincia);
    }
    Localidad localidad = this.getLocalidadPorNombre(nombre, provincia);
    if (localidad == null) {
      localidad = new Localidad();
      localidad.setNombre(nombre);
      localidad.setCodigoPostal(codigoPostal);
      localidad.setProvincia(provincia);
      localidad = localidadRepository.save(localidad);
    }
    return localidad;
  }

  @Override
  @Transactional
  public void actualizarUbicacionEnvio(Ubicacion ubicacionParaActualizar, Cliente cliente) {
    Ubicacion ubicacionDeEnvio = cliente.getUbicacionEnvio();
    this.actualizarUbicacion(ubicacionDeEnvio, ubicacionParaActualizar);
  }

  @Override
  @Transactional
  public void actualizarUbicacionFacturacion(Ubicacion ubicacionParaActualizar, Cliente cliente) {
    Ubicacion ubicacionDeFacturacion = cliente.getUbicacionFacturacion();
    this.actualizarUbicacion(ubicacionDeFacturacion, ubicacionParaActualizar);
  }

  private void actualizarUbicacion(Ubicacion ubicacionCliente, Ubicacion ubicacionParaActualizar) {
    ubicacionCliente.setCalle(ubicacionParaActualizar.getCalle());
    ubicacionCliente.setDepartamento(ubicacionParaActualizar.getDepartamento());
    ubicacionCliente.setDescripcion(ubicacionParaActualizar.getDescripcion());
    ubicacionCliente.setLatitud(ubicacionParaActualizar.getLatitud());
    ubicacionCliente.setLongitud(ubicacionParaActualizar.getLongitud());
    ubicacionCliente.setNumero(ubicacionParaActualizar.getNumero());
    ubicacionCliente.setCalle(ubicacionParaActualizar.getCalle());
    ubicacionCliente.setPiso(ubicacionParaActualizar.getPiso());
    //ubicacionCliente.setEliminada(false);
    ubicacionRepository.save(ubicacionParaActualizar);
  }

  @Override
  public Page<Ubicacion> buscarUbicaciones(BusquedaUbicacionCriteria criteria) {
    QUbicacion qUbicacion = QUbicacion.ubicacion;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscarPorDescripcion()) {
      String[] terminos = criteria.getDescripcion().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qUbicacion.descripcion.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorLocalidad())
      builder.and(qUbicacion.localidad.id_Localidad.eq(criteria.getIdLocalidad()));
    if (criteria.isBuscaPorProvincia())
      builder.and(qUbicacion.localidad.provincia.id_Provincia.eq(criteria.getIdProvincia()));
    if (criteria.isBuscaPorCodigoPostal())
      builder.and(qUbicacion.localidad.codigoPostal.eq(criteria.getCodigoPostal()));
    return ubicacionRepository.findAll(builder, criteria.getPageable());
  }

  @Override
  public void eliminarUbicacion(long idUbicacion) {
    ubicacionRepository.eliminar(idUbicacion);
  }

  @Override
  public Ubicacion getUbicacionPorId(long idUbicacion) {
    return ubicacionRepository.findById(idUbicacion);
  }

  @Override
  public Localidad getLocalidadPorId(Long idLocalidad) {
    Localidad localidad = localidadRepository.findOne(idLocalidad);
    if (localidad == null) {
      throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
        .getString("mensaje_localidad_no_existente"));
    }
    return localidad;
  }

  @Override
  public Localidad getLocalidadPorNombre(String nombre, Provincia provincia) {
    return localidadRepository.findByNombreAndProvinciaAndEliminadaOrderByNombreAsc(nombre, provincia, false);
  }

  @Override
  public List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia) {
    return localidadRepository.findAllByAndProvinciaAndEliminadaOrderByNombreAsc(provincia, false);
  }


  @Override
  public Provincia getProvinciaPorId(Long idProvincia) {
    Provincia provincia = provinciaRepository.findOne(idProvincia);
    if (provincia == null) {
      throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
        .getString("mensaje_provincia_no_existente"));
    }
    return provincia;
  }

  @Override
  public Provincia getProvinciaPorNombre(String nombre) {
    return provinciaRepository.findByNombreAndEliminadaOrderByNombreAsc(nombre, false);
  }

  @Override
  public List<Provincia> getProvincias() {
    return provinciaRepository.findAllByAndEliminadaOrderByNombreAsc(false);
  }
}
