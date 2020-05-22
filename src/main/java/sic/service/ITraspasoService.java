package sic.service;

import sic.modelo.Pedido;
import sic.modelo.Traspaso;
import sic.modelo.dto.NuevoTraspasoDTO;

import java.util.List;

public interface ITraspasoService {

  Traspaso getTraspasoNoEliminadoPorid(Long idTraspaso);

  Traspaso guardar(NuevoTraspasoDTO nuevoTraspasoDTO);

  List<Traspaso> guardarTraspasosPorPedido(Pedido pedido);

  List<NuevoTraspasoDTO> construirNuevosTraspasosPorPedido(Pedido pedido);

  void eliminar(Long idTraspaso);

  String generarNroDeTraspaso();
}
