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
  @Transactional
  public Ubicacion getUbicacionPorId(long idUbicacion) {
    return ubicacionRepository.findById(idUbicacion);
  }

  @Override
  @Transactional
  public Ubicacion guardar(
    Ubicacion ubicacion) {
    Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
    logger.warn("La ubicación {} se actualizó correctamente.", ubicacion);
    return ubicacionGuardada;
  }

  @Override
  public Localidad getLocalidadPorId(Long idLocalidad) {
    Localidad localidad = localidadRepository.findOne(idLocalidad);
    if (localidad == null) {
      throw new EntityNotFoundException(
        RESOURCE_BUNDLE.getString("mensaje_localidad_no_existente"));
    }
    return localidad;
  }

  @Override
  public Localidad getLocalidadPorNombre(String nombre, Provincia provincia) {
    return localidadRepository.findByNombreAndProvinciaOrderByNombreAsc(nombre, provincia);
  }

  @Override
  public Localidad getLocalidadPorCodigoPostal(String codigoPostal) {
    return localidadRepository.findByCodigoPostal(codigoPostal);
  }

  @Override
  public List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia) {
    return localidadRepository.findAllByAndProvinciaOrderByNombreAsc(provincia);
  }

  @Override
  public Provincia getProvinciaPorId(Long idProvincia) {
    Provincia provincia = provinciaRepository.findOne(idProvincia);
    if (provincia == null) {
      throw new EntityNotFoundException(
        ResourceBundle.getBundle("Mensajes").getString("mensaje_provincia_no_existente"));
    }
    return provincia;
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
    // Codigo Postal
    if (localidad.getCodigoPostal() != null) {
      localidadDuplicada = this.getLocalidadPorCodigoPostal(localidad.getCodigoPostal());
      if (operacion.equals(TipoDeOperacion.ALTA) && localidadDuplicada != null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes")
                .getString("mensaje_localidad_duplicado_codigo_postal"));
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
