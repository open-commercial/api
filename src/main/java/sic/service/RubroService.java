package sic.service;

import java.util.List;

import sic.modelo.Rubro;
import sic.modelo.TipoDeOperacion;

public interface RubroService {

  Rubro getRubroNoEliminadoPorId(Long idRubro);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Rubro rubro);

  void actualizar(Rubro rubro);

  void eliminar(long idRubro);

  Rubro getRubroPorNombre(String nombre);

  List<Rubro> getRubros();

  Rubro guardar(Rubro rubro);
}
