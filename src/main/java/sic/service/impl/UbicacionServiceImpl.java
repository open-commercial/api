package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.BusquedaUbicacionCriteria;
import sic.modelo.Cliente;
import sic.modelo.QUbicacion;
import sic.modelo.Ubicacion;
import sic.repository.UbicacionRepository;
import sic.service.IUbicacionService;

@Service
public class UbicacionServiceImpl implements IUbicacionService {

  private final UbicacionRepository ubicacionRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public UbicacionServiceImpl(
    UbicacionRepository ubicacionRepository) {
    this.ubicacionRepository= ubicacionRepository;
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
  public void eliminar(long idUbicacion) {
    ubicacionRepository.eliminar(idUbicacion);
  }

  @Override
  public Ubicacion getUbicacionPorId(long idUbicacion) {
    return  ubicacionRepository.findById(idUbicacion);
  }
}
