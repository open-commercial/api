package sic.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevoRenglonPedidoDTO;

import javax.validation.Valid;

public interface IPedidoService {

  Pedido getPedidoNoEliminadoPorId(long idPedido);

  void actualizar(@Valid Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal);

  void actualizarFacturasDelPedido(@Valid Pedido pedido, List<Factura> facturas);

  Page<Pedido> buscarPedidos(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn);

  long generarNumeroPedido(Empresa empresa);

  Pedido actualizarEstadoPedido(Pedido pedido);

  Pedido calcularTotalActualDePedido(Pedido pedido);

  boolean eliminar(long idPedido);

  List<Factura> getFacturasDelPedido(long id);

  Map<Long, RenglonFactura> getRenglonesFacturadosDelPedido(long nroPedido);

  List<RenglonPedido> getRenglonesDelPedido(Long idPedido);

  byte[] getReportePedido(Pedido pedido);

  Pedido guardar(@Valid Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal);

  RenglonPedido calcularRenglonPedido(
    long idProducto,
    BigDecimal cantidad,
    BigDecimal descuentoPorcentaje);

  List<RenglonPedido> calcularRenglonesPedido(List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO);

  BigDecimal calcularDescuentoNeto(BigDecimal precioUnitario, BigDecimal descuentoPorcentaje);

}
