package sic.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;

import javax.validation.Valid;

public interface IPedidoService {

  Pedido getPedidoNoEliminadoPorId(long idPedido);

  void actualizar(Pedido pedido);

  void actualizarFacturasDelPedido(@Valid Pedido pedido, List<Factura> facturas);

  Page<Pedido> buscarPedidos(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  long generarNumeroPedido(Sucursal sucursal);

  void actualizarEstadoPedido(Pedido pedido);

  Pedido calcularTotalActualDePedido(Pedido pedido);

  boolean eliminar(long idPedido);

  List<Factura> getFacturasDelPedido(long id);

  Map<Long, BigDecimal> getRenglonesFacturadosDelPedido(long nroPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglonAndProductosNoEliminados(Long idPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglon(Long idPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstado(Long idPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadoPorIdProducto(Long idPedido);

  byte[] getReportePedido(long idPedido);

  Pedido guardar(Pedido pedido);

  RenglonPedido calcularRenglonPedido(long idProducto, BigDecimal cantidad);

  List<RenglonPedido> calcularRenglonesPedido(long[] idProductoItem, BigDecimal[] cantidad);

  Resultados calcularResultadosPedido(NuevosResultadosComprobanteDTO calculoPedido);

  Pedido getPedidoPorIdPayment(String idPayment);

  long[] getArrayDeIdProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones);

  BigDecimal[] getArrayDeCantidadesProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones);
}
