package org.opencommercial.service;

import java.util.List;

import org.opencommercial.model.Rubro;
import org.opencommercial.model.TipoDeOperacion;

public interface RubroService {

  Rubro getRubroNoEliminadoPorId(Long idRubro);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Rubro rubro);

  void actualizar(Rubro rubro);

  void eliminar(long idRubro);

  Rubro getRubroPorNombre(String nombre);

  List<Rubro> getRubros();

  Rubro guardar(Rubro rubro);
}
