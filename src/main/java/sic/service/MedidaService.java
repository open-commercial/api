package sic.service;

import sic.modelo.TipoDeOperacion;
import java.util.List;
import sic.modelo.Medida;

public interface MedidaService {

  Medida getMedidaNoEliminadaPorId(Long idMedida);

  void actualizar(Medida medida);

  void eliminar(long idMedida);

  Medida getMedidaPorNombre(String nombre);

  List<Medida> getUnidadMedidas();

  Medida guardar(Medida medida);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Medida medida);
}
