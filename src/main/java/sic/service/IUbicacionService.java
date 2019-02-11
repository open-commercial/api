package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaUbicacionCriteria;
import sic.modelo.Cliente;
import sic.modelo.Ubicacion;

public interface IUbicacionService {

  Ubicacion guardar(Ubicacion ubicacion);

  void actualizarUbicacionEnvio(Ubicacion ubicacion, Cliente cliente);

  Page<Ubicacion> buscarUbicaciones(BusquedaUbicacionCriteria criteria);

  void eliminar(long idUbicacion);

  Ubicacion getUbicacionPorId(long idUbicacion);
}
