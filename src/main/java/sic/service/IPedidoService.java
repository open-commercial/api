package sic.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.domain.Page;
import sic.modelo.*;

public interface IPedidoService {

  Pedido getPedidoPorId(Long id);

  void actualizar(Pedido pedido);

  Page<Pedido> buscarConCriteria(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  long calcularNumeroPedido(Empresa empresa);

  Pedido actualizarEstadoPedido(Pedido pedido);

  Pedido calcularTotalActualDePedido(Pedido pedido);

  boolean eliminar(long idPedido);

  List<Factura> getFacturasDelPedido(long id);

  HashMap<Long, RenglonFactura> getRenglonesFacturadosDelPedido(long nroPedido);

  List<RenglonPedido> getRenglonesDelPedido(Long idPedido);

  byte[] getReportePedido(Pedido pedido);

  Pedido guardar(Pedido pedido);

  RenglonPedido calcularRenglonPedido(
      long idProducto,
      BigDecimal cantidad,
      BigDecimal descuentoPorcentaje);

  List<RenglonPedido> convertirRenglonesFacturaEnRenglonesPedido(List<RenglonFactura> renglonesFactura);

  BigDecimal calcularDescuentoNeto(BigDecimal precioUnitario, BigDecimal descuentoPorcentaje);

  BigDecimal calcularSubTotal(BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuentoNeto);

}
