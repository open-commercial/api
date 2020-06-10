package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.Pedido;
import sic.modelo.RenglonTraspaso;
import sic.modelo.Traspaso;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;

import java.util.List;

public interface ITraspasoService {

  Traspaso getTraspasoNoEliminadoPorid(Long idTraspaso);

  List<RenglonTraspaso> getRenglonesTraspaso(Long idTraspaso);

  Traspaso guardar(NuevoTraspasoDTO nuevoTraspasoDTO);

  List<Traspaso> guardarTraspasosPorPedido(Pedido pedido);

  List<NuevoTraspasoDTO> construirNuevosTraspasosPorPedido(Pedido pedido);

  void eliminar(Long idTraspaso);

  String generarNroDeTraspaso();

  Page<Traspaso> buscarTraspasos(BusquedaTraspasoCriteria criteria);
}
