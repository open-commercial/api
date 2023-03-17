package sic.service;

import java.math.BigDecimal;
import java.util.List;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.CantidadProductoDTO;
import sic.modelo.dto.CommitDTO;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;

public interface IPedidoService {

  void validarReglasDeNegocio(TipoDeOperacion operacion, Pedido pedido);

  Pedido getPedidoNoEliminadoPorId(long idPedido);

  Pedido getPedidoPorNumeroAndSucursal(long nroPedido, Sucursal sucursal);

  void actualizar(Pedido pedido, List<CantidadProductoDTO> renglonesAnteriores, Long idSucursal, List<Recibo> recibos);

  void actualizarFacturasDelPedido(Pedido pedido, List<Factura> facturas);

  Page<Pedido> buscarPedidos(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  BooleanBuilder getBuilderPedido(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  long generarNumeroPedido(Sucursal sucursal);

  void cancelar(Pedido pedido);

  void eliminar(long idPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglon(Long idPedido);

  List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstadoOrClonar(Long idPedido, boolean clonar);

  byte[] getReportePedido(long idPedido);

  Pedido guardar(Pedido pedido, List<Recibo> recibos);

  void cambiarFechaDeVencimiento(long idPedido);

  RenglonPedido calcularRenglonPedido(long idProducto, BigDecimal cantidad);

  List<RenglonPedido> calcularRenglonesPedido(long[] idProductoItem, BigDecimal[] cantidad);

  Resultados calcularResultadosPedido(NuevosResultadosComprobanteDTO calculoPedido);

  long[] getArrayDeIdProducto(List<CantidadProductoDTO> nuevosRenglones);

  BigDecimal[] getArrayDeCantidadesProducto(List<CantidadProductoDTO> nuevosRenglones);

  void actualizarCantidadReservadaDeProductosPorCambioDeEstado(Pedido pedido);

  void actualizarCantidadReservadaDeProductosPorModificacion(Pedido pedido, List<CantidadProductoDTO> renglonesAnteriores);

  List<RenglonPedido> actualizarRenglonesPedido(List<RenglonPedido> renglonesDelPedido, List<CantidadProductoDTO> renglonesParaActualizar);

  RenglonPedido actualizarCantidadRenglonPedido(RenglonPedido renglonPedido, BigDecimal cantidadNueva);

  List<CommitDTO> getPedidoChanges(long idPedido);
}
