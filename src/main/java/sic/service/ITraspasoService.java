package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.entity.Pedido;
import sic.entity.RenglonTraspaso;
import sic.entity.Traspaso;
import sic.entity.criteria.BusquedaTraspasoCriteria;
import sic.dto.NuevoTraspasoDTO;
import sic.dto.NuevoTraspasoDePedidoDTO;

import java.util.List;

public interface ITraspasoService {

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
