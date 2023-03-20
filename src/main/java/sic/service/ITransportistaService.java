package sic.service;

import java.util.List;

import sic.domain.TipoDeOperacion;
import sic.entity.criteria.BusquedaTransportistaCriteria;
import org.springframework.data.domain.Page;
import sic.entity.Transportista;

public interface ITransportistaService {

  Transportista getTransportistaNoEliminadoPorId(long idTransportista);

  void actualizar(Transportista transportista);

  Page<Transportista> buscarTransportistas(BusquedaTransportistaCriteria criteria);

  void eliminar(long idTransportista);

  Transportista getTransportistaPorNombre(String nombre);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Transportista transportista);

  List<Transportista> getTransportistas();

  Transportista guardar(Transportista transportista);
}
