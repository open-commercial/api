package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.repository.LocalidadRepository;
import sic.repository.ProvinciaRepository;
import sic.repository.UbicacionRepository;
import sic.service.*;
import sic.exception.BusinessServiceException;
import sic.util.Validator;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;

@Service
@Validated
public class UbicacionServiceImpl implements IUbicacionService {

  private final UbicacionRepository ubicacionRepository;
  private final LocalidadRepository localidadRepository;
  private final ProvinciaRepository provinciaRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public UbicacionServiceImpl(
      UbicacionRepository ubicacionRepository,
      LocalidadRepository localidadRepository,
      ProvinciaRepository provinciaRepository,
      MessageSource messageSource) {
    this.ubicacionRepository = ubicacionRepository;
    this.localidadRepository = localidadRepository;
    this.provinciaRepository = provinciaRepository;
    this.messageSource = messageSource;
  }

  @Override
  @Transactional
  public Ubicacion getUbicacionPorId(long idUbicacion) {
    return ubicacionRepository.findById(idUbicacion).orElse(null);
  }

  @Override
  @Transactional
  public Ubicacion guardar(@Valid Ubicacion ubicacion) {
    Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
    logger.warn("La ubicación {} se actualizó correctamente.", ubicacion);
    return ubicacionGuardada;
  }

  @Override
  public Localidad getLocalidadPorId(Long idLocalidad) {
    return localidadRepository
        .findById(idLocalidad)
        .orElseThrow(
            () ->
                new EntityNotFoundException(messageSource.getMessage(
                  "mensaje_localidad_no_existente", null, Locale.getDefault())));
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
                new EntityNotFoundException(messageSource.getMessage(
                  "mensaje_provincia_no_existente", null, Locale.getDefault())));
  }

  @Override
  public List<Provincia> getProvincias() {
    return provinciaRepository.findAllByOrderByNombreAsc();
  }

  @Override
  @Transactional
  public void actualizarLocalidad(@Valid Localidad localidad) {
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, localidad);
    localidadRepository.save(localidad);
  }

  @Override
  public void validarOperacion(TipoDeOperacion operacion, Localidad localidad) {
    // Requeridos
    if (Validator.esVacio(localidad.getNombre())) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_localidad_vacio_nombre", null, Locale.getDefault()));
    }
    if (localidad.getProvincia() == null) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_localidad_provincia_vacio", null, Locale.getDefault()));
    }
    // Duplicados
    // Nombre
    Localidad localidadDuplicada =
        this.getLocalidadPorNombre(localidad.getNombre(), localidad.getProvincia());
    if (operacion.equals(TipoDeOperacion.ALTA) && localidadDuplicada != null) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_localidad_duplicado_nombre", null, Locale.getDefault()));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
        && localidadDuplicada != null
        && localidadDuplicada.getIdLocalidad() != localidad.getIdLocalidad()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_localidad_duplicado_nombre", null, Locale.getDefault()));
    }
  }

  @Override
  public Page<Localidad> buscar(BusquedaLocalidadCriteria criteria) {
    QLocalidad qLocalidad = QLocalidad.localidad;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getNombre() != null) {
      String[] terminos = criteria.getNombre().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qLocalidad.nombre.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getCodigoPostal() != null) {
      String[] terminos = criteria.getCodigoPostal().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qLocalidad.codigoPostal.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getNombre() != null) {
      String[] terminos = criteria.getNombreProvincia().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qLocalidad.provincia.nombre.containsIgnoreCase(termino));
      }
      builder.and(rsPredicate);
    }
    if (criteria.getEnvioGratuito() != null) {
      builder.and(qLocalidad.envioGratuito.eq(criteria.getEnvioGratuito()));
    }
    return localidadRepository.findAll(
        builder,
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(int pagina, String ordenarPor, String sentido) {
    String ordenDefault = "nombre";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
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
}
