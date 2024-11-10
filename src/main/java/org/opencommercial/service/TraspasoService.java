package org.opencommercial.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.opencommercial.model.Pedido;
import org.opencommercial.model.RenglonTraspaso;
import org.opencommercial.model.Traspaso;
import org.opencommercial.model.criteria.BusquedaTraspasoCriteria;
import org.opencommercial.model.dto.NuevoTraspasoDTO;
import org.opencommercial.model.dto.NuevoTraspasoDePedidoDTO;

import java.util.List;

public interface TraspasoService {

  Traspaso getTraspasoNoEliminadoPorid(Long idTraspaso);

  List<RenglonTraspaso> getRenglonesTraspaso(Long idTraspaso);

  Traspaso guardarTraspasoDePedido(NuevoTraspasoDePedidoDTO nuevoTraspasoDePedidoDTO);

  Traspaso guardarTraspaso(NuevoTraspasoDTO nuevoTraspasoDTO, long idUsuario);

  List<Traspaso> guardarTraspasosPorPedido(Pedido pedido);

  void eliminarTraspasoDePedido(Pedido pedido);

  List<NuevoTraspasoDePedidoDTO> construirNuevosTraspasosPorPedido(Pedido pedido);

  void eliminar(Long idTraspaso);

  String generarNroDeTraspaso();

  Page<Traspaso> buscarTraspasos(BusquedaTraspasoCriteria criteria);

  BooleanBuilder getBuilderTraspaso(BusquedaTraspasoCriteria criteria);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido, int tamanioPagina);

  byte[] getReporteTraspaso(BusquedaTraspasoCriteria criteria);
}
