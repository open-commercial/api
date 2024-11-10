package org.opencommercial.service;

import org.springframework.data.domain.Page;
import org.opencommercial.model.Localidad;
import org.opencommercial.model.Provincia;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.Ubicacion;
import org.opencommercial.model.criteria.BusquedaLocalidadCriteria;
import org.opencommercial.model.dto.LocalidadesParaActualizarDTO;

import java.util.List;

public interface UbicacionService {

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
