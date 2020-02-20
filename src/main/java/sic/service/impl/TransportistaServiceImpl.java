package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.criteria.BusquedaTransportistaCriteria;
import sic.service.ITransportistaService;
import sic.exception.BusinessServiceException;
import sic.repository.TransportistaRepository;
import sic.service.IUbicacionService;

@Service
@Validated
public class TransportistaServiceImpl implements ITransportistaService {

  private final TransportistaRepository transportistaRepository;
  private final IUbicacionService ubicacionService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;

  @Autowired
  public TransportistaServiceImpl(TransportistaRepository transportistaRepository,
                                  IUbicacionService ubicacionService,
                                  MessageSource messageSource) {
    this.transportistaRepository = transportistaRepository;
    this.ubicacionService = ubicacionService;
    this.messageSource = messageSource;
  }

  @Override
  public Transportista getTransportistaNoEliminadoPorId(long idTransportista) {
    Optional<Transportista> transportista = transportistaRepository
      .findById(idTransportista);
    if (transportista.isPresent() && !transportista.get().isEliminado()) {
      return transportista.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_transportista_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public List<Transportista> getTransportistas(Sucursal sucursal) {
    List<Transportista> transportista =
        transportistaRepository.findAllByAndEliminadoOrderByNombreAsc(false);
    if (transportista == null) {
      throw new EntityNotFoundException(
          messageSource.getMessage(
              "mensaje_transportista_ninguno_cargado", null, Locale.getDefault()));
    }
    return transportista;
  }

  @Override
  public Page<Transportista> buscarTransportistas(BusquedaTransportistaCriteria criteria) {
    QTransportista qTransportista = QTransportista.transportista;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qTransportista.eliminado.eq(false));
    if (criteria.getNombre() != null)
      builder.and(this.buildPredicadoNombre(criteria.getNombre(), qTransportista));
    if (criteria.getIdLocalidad() != null)
      builder.and(qTransportista.ubicacion.localidad.idLocalidad.eq(criteria.getIdLocalidad()));
    if (criteria.getIdProvincia() != null)
      builder.and(
          qTransportista.ubicacion.localidad.provincia.idProvincia.eq(criteria.getIdProvincia()));
    return transportistaRepository.findAll(
        builder,
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "nombre";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
        pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenDefault));
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

  private BooleanBuilder buildPredicadoNombre(String nombre, QTransportista qTransportista) {
    String[] terminos = nombre.split(" ");
    BooleanBuilder descripcionProducto = new BooleanBuilder();
    for (String termino : terminos) {
      descripcionProducto.and(qTransportista.nombre.containsIgnoreCase(termino));
    }
    return descripcionProducto;
  }

  @Override
  public Transportista getTransportistaPorNombre(String nombre) {
    return transportistaRepository.findByNombreAndEliminado(nombre, false);
  }

  private void validarOperacion(TipoDeOperacion operacion, Transportista transportista) {
    // Duplicados
    // Nombre
    Transportista transportistaDuplicado = this.getTransportistaPorNombre(transportista.getNombre());
    if (operacion.equals(TipoDeOperacion.ALTA) && transportistaDuplicado != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_transportista_duplicado_nombre", null, Locale.getDefault()));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
        && (transportistaDuplicado != null
            && transportistaDuplicado.getIdTransportista()
                != transportista.getIdTransportista())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_transportista_duplicado_nombre", null, Locale.getDefault()));
    }
    if (transportista.getUbicacion() != null
        && transportista.getUbicacion().getLocalidad() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_sin_localidad", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public Transportista guardar(@Valid Transportista transportista) {
    if (transportista.getUbicacion() != null
        && transportista.getUbicacion().getIdLocalidad() != null) {
      transportista
          .getUbicacion()
          .setLocalidad(
              ubicacionService.getLocalidadPorId(transportista.getUbicacion().getIdLocalidad()));
    }
    this.validarOperacion(TipoDeOperacion.ALTA, transportista);
    transportista = transportistaRepository.save(transportista);
    logger.warn("El Transportista {} se guardó correctamente.", transportista);
    return transportista;
  }

  @Override
  @Transactional
  public void actualizar(@Valid Transportista transportista) {
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, transportista);
    transportistaRepository.save(transportista);
  }

  @Override
  @Transactional
  public void eliminar(long idTransportista) {
    Transportista transportista = this.getTransportistaNoEliminadoPorId(idTransportista);
    if (transportista == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_transportista_no_existente", null, Locale.getDefault()));
    }
    transportista.setEliminado(true);
    transportista.setUbicacion(null);
    transportistaRepository.save(transportista);
  }
}
