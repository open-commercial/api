package sic.service;

import sic.domain.TipoDeOperacion;
import java.util.List;
import sic.entity.Medida;

public interface IMedidaService {

  Medida getMedidaNoEliminadaPorId(Long idMedida);

  void actualizar(Medida medida);

  void eliminar(long idMedida);

  Medida getMedidaPorNombre(String nombre);

  List<Medida> getUnidadMedidas();

  Medida guardar(Medida medida);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Medida medida);
}
