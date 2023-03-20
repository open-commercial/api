package sic.service;

import java.util.List;

import sic.entity.Rubro;
import sic.domain.TipoDeOperacion;

public interface IRubroService {

  Rubro getRubroNoEliminadoPorId(Long idRubro);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Rubro rubro);

  void actualizar(Rubro rubro);

  void eliminar(long idRubro);

  Rubro getRubroPorNombre(String nombre);

  List<Rubro> getRubros();

  Rubro guardar(Rubro rubro);
}
