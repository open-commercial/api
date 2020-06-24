package sic.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;

public interface IPedidoService {

  void validarReglasDeNegocio(TipoDeOperacion operacion, Pedido pedido);

  Pedido getPedidoNoEliminadoPorId(long idPedido);

  void actualizar(Pedido pedido, List<RenglonPedido> renglonesAnteriores, List<Recibo> recibos);

  void actualizarFacturasDelPedido(Pedido pedido, List<Factura> facturas);

  Page<Pedido> buscarPedidos(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  BooleanBuilder getBuilderPedido(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  long generarNumeroPedido(Sucursal sucursal);

  Pedido calcularTotalActualDePedido(Pedido pedido);

  void cancelar(Pedido pedido);

  void eliminar(long idPedido);

  List<Factura> getFacturasDelPedido(long id);

  Map<Long, BigDecimal> getRenglonesFacturadosDelPedido(long nroPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglon(Long idPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstadoOrClonar(Long idPedido, boolean clonar);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadoPorIdProducto(Long idPedido);

  byte[] getReportePedido(long idPedido);

  Pedido guardar(Pedido pedido, List<Recibo> recibos);

  void cambiarFechaDeVencimiento(long idPedido);

  RenglonPedido calcularRenglonPedido(long idProducto, BigDecimal cantidad);

  List<RenglonPedido> calcularRenglonesPedido(long[] idProductoItem, BigDecimal[] cantidad);

  Resultados calcularResultadosPedido(NuevosResultadosComprobanteDTO calculoPedido);

  long[] getArrayDeIdProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones);

  BigDecimal[] getArrayDeCantidadesProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones);
}
