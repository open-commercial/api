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
import sic.modelo.*;
import sic.modelo.criteria.BusquedaLocalidadCriteria;
import sic.modelo.dto.LocalidadesParaActualizarDTO;
import sic.repository.LocalidadRepository;
import sic.repository.ProvinciaRepository;
import sic.repository.UbicacionRepository;
import sic.service.*;
import sic.exception.BusinessServiceException;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UbicacionServiceImpl implements IUbicacionService {

  private final UbicacionRepository ubicacionRepository;
  private final LocalidadRepository localidadRepository;
  private final ProvinciaRepository provinciaRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final CustomValidator customValidator;

  @Autowired
  public UbicacionServiceImpl(
    UbicacionRepository ubicacionRepository,
    LocalidadRepository localidadRepository,
    ProvinciaRepository provinciaRepository,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.ubicacionRepository = ubicacionRepository;
    this.localidadRepository = localidadRepository;
    this.provinciaRepository = provinciaRepository;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  @Transactional
  public Ubicacion getUbicacionPorId(long idUbicacion) {
    return ubicacionRepository.findById(idUbicacion).orElse(null);
  }

  @Override
  @Transactional
  public Ubicacion guardar(Ubicacion ubicacion) {
    customValidator.validar(ubicacion);
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
  public void actualizarLocalidad(Localidad localidad) {
    customValidator.validar(localidad);
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, localidad);
    localidadRepository.save(localidad);
  }

  @Override
  public void actualizarMultiplesLocalidades(
      LocalidadesParaActualizarDTO localidadesParaActualizar) {
    List<Localidad> localidadesParaModificar = new ArrayList<>();
    for (int i = 0; i < localidadesParaActualizar.getIdLocalidad().length; ++i) {
      localidadesParaModificar.add(
          localidadRepository.findById(localidadesParaActualizar.getIdLocalidad()[i]));
    }
    localidadesParaModificar.forEach(
        localidad -> {
          localidad.setEnvioGratuito(localidadesParaActualizar.isEnvioGratuito());
          if (localidadesParaActualizar.getCostoDeEnvio() != null) {
            localidad.setCostoEnvio(localidadesParaActualizar.getCostoDeEnvio());
          }
        });
    localidadesParaModificar.forEach(this::actualizarLocalidad);
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Localidad localidad) {
    // Requeridos
    if (localidad.getNombre() == null || localidad.getNombre().equals("")) {
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
  public Page<Localidad> buscarLocalidades(BusquedaLocalidadCriteria criteria) {
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
    if (criteria.getNombreProvincia() != null) {
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

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "nombre";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }
}
