package org.opencommercial.service;

import java.util.List;

import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.criteria.BusquedaTransportistaCriteria;
import org.springframework.data.domain.Page;
import org.opencommercial.model.Transportista;

public interface TransportistaService {

  Transportista getTransportistaNoEliminadoPorId(long idTransportista);

  void actualizar(Transportista transportista);

  Page<Transportista> buscarTransportistas(BusquedaTransportistaCriteria criteria);

  void eliminar(long idTransportista);

  Transportista getTransportistaPorNombre(String nombre);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Transportista transportista);

  List<Transportista> getTransportistas();

  Transportista guardar(Transportista transportista);
}
