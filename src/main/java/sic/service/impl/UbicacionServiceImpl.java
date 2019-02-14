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
    Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
    logger.warn("El Cliente {} se actualizó correctamente.", ubicacion);
    return ubicacionGuardada;
  }

  @Override
  @Transactional
  public void actualizarUbicacionEnvio(Ubicacion ubicacion, Cliente cliente) {
    Ubicacion ubicacionDeEnvio = cliente.getUbicacionEnvio();
    ubicacionDeEnvio.setLocalidad(ubicacion.getLocalidad());
    ubicacionDeEnvio.setCalle(ubicacion.getCalle());
    ubicacionDeEnvio.setDepartamento(ubicacion.getDepartamento());
    ubicacionDeEnvio.setDescripcion(ubicacion.getDescripcion());
    ubicacionDeEnvio.setLatitud(ubicacion.getLatitud());
    ubicacionDeEnvio.setLongitud(ubicacion.getLongitud());
    ubicacionDeEnvio.setNumero(ubicacion.getNumero());
    ubicacionDeEnvio.setCalle(ubicacion.getCalle());
    ubicacionDeEnvio.setPiso(ubicacion.getPiso());
    ubicacionDeEnvio.setEliminada(false);
    ubicacionRepository.save(ubicacionDeEnvio);
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
