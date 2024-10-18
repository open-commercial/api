package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.Pedido;
import sic.modelo.RenglonTraspaso;
import sic.modelo.Traspaso;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.modelo.dto.NuevoTraspasoDePedidoDTO;

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
