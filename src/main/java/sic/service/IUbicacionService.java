package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaUbicacionCriteria;
import sic.modelo.Ubicacion;

public interface IUbicacionService {

  void actualizar(Ubicacion ubicacion);

  Page<Ubicacion> buscarUbicaciones(BusquedaUbicacionCriteria criteria);

  void eliminar(long idUbicacion);

  Ubicacion getUbicacionPorId(long idUbicacion);
}
