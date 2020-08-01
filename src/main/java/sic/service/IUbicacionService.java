package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaLocalidadCriteria;
import sic.modelo.dto.LocalidadesParaActualizarDTO;

import java.util.List;

public interface IUbicacionService {

  Ubicacion getUbicacionPorId(long idUbicacion);

  Page<Localidad> buscarLocalidades(BusquedaLocalidadCriteria criteria);

  Ubicacion guardar(Ubicacion ubicacion);

  Localidad getLocalidadPorId(Long idLocalidad);

  Localidad getLocalidadPorNombre(String nombre, Provincia provincia);

  List<Localidad> getLocalidadesDeLaProvincia(Provincia provincia);

  Provincia getProvinciaPorId(Long idProvincia);

  List<Provincia> getProvincias();

  void actualizarLocalidad(Localidad localidad);

  void actualizarMultiplesLocalidades(LocalidadesParaActualizarDTO localidadesParaActualizar);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Localidad localidad);
}
