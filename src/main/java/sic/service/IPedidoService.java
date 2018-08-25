package sic.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.dto.NuevoRenglonPedidoDTO;

public interface IPedidoService {

  Pedido getPedidoPorId(Long id);

  void actualizar(Pedido pedido);

  Page<Pedido> buscarConCriteria(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  long calcularNumeroPedido(Empresa empresa);

  Pedido actualizarEstadoPedido(Pedido pedido);

  Pedido calcularTotalActualDePedido(Pedido pedido);

  boolean eliminar(long idPedido);

  List<Factura> getFacturasDelPedido(long id);

  Map<Long, RenglonFactura> getRenglonesFacturadosDelPedido(long nroPedido);

  List<RenglonPedido> getRenglonesDelPedido(Long idPedido);

  byte[] getReportePedido(Pedido pedido);

  Pedido guardar(Pedido pedido);

  RenglonPedido calcularRenglonPedido(
      long idProducto,
      BigDecimal cantidad,
      BigDecimal descuentoPorcentaje);

  List<RenglonPedido> calcularRenglonesPedido(List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO);

  BigDecimal calcularDescuentoNeto(BigDecimal precioUnitario, BigDecimal descuentoPorcentaje);

  BigDecimal calcularSubTotal(BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuentoNeto);

}
