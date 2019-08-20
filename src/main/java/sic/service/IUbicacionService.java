package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;

import javax.validation.Valid;
import java.util.List;

public interface IUbicacionService {

  Ubicacion getUbicacionPorId(long idUbicacion);

  Page<Localidad> buscar(BusquedaLocalidadCriteria criteria);

  Ubicacion guardar(
    @Valid Ubicacion ubicacion);

  Localidad getLocalidadPorId(Long idLocalidad);

  Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

  List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

  Provincia getProvinciaPorId(Long idProvincia);

  List<Provincia> getProvincias();

  void actualizarLocalidad(@Valid Localidad localidad);

  void validarOperacion(TipoDeOperacion operacion, Localidad localidad);
}
