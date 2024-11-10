package org.opencommercial.service;

import org.opencommercial.model.TipoDeOperacion;
import java.util.List;
import org.opencommercial.model.Medida;

public interface MedidaService {

  Medida getMedidaNoEliminadaPorId(Long idMedida);

  void actualizar(Medida medida);

  void eliminar(long idMedida);

  Medida getMedidaPorNombre(String nombre);

  List<Medida> getUnidadMedidas();

  Medida guardar(Medida medida);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Medida medida);
}
