package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.repository.PedidoRepository;
import sic.repository.RenglonPedidoRepository;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {PedidoServiceImpl.class, CustomValidator.class, MessageSource.class})
class PedidoServiceImplTest {

  @MockBean PedidoRepository pedidoRepository;
  @MockBean RenglonPedidoRepository renglonPedidoRepository;
  @MockBean FacturaVentaServiceImpl facturaVentaService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean CorreoElectronicoServiceImpl correoElectronicoService;
  @MockBean ConfiguracionSucursalServiceImpl configuracionSucursalService;
  @MockBean CuentaCorrienteServiceImpl cuentaCorrienteService;
  @MockBean MessageSource messageSource;
  @MockBean ModelMapper modelMapper;

  @Autowired PedidoServiceImpl pedidoService;

  @Test
  void shouldEliminarPedidoAbierto() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    pedidoService.eliminar(1L);
    verify(pedidoRepository, times(1)).save(pedido);
  }

  @Test
  void shouldEliminarPedidoCerrado() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CERRADO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    assertThrows(BusinessServiceException.class, () -> pedidoService.eliminar(1L));
    verify(messageSource).getMessage(eq("mensaje_no_se_puede_eliminar_pedido"), any(), any());
  }

  @Test
  void shouldCalcularTotalActualDePedido() {
    Pedido pedido = new Pedido();
    pedido.setIdPedido(1L);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    Cliente cliente = new Cliente();
    cliente.setSaldoCuentaCorriente(BigDecimal.ZERO);
    pedido.setCliente(cliente);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    List<Producto> productos = new ArrayList<>();
    Producto producto = new Producto();
    producto.setOferta(true); // primercaso
    producto.setBulto(BigDecimal.ONE);
    producto.setPorcentajeBonificacionOferta(BigDecimal.TEN);
    producto.setPorcentajeBonificacionPrecio(BigDecimal.ONE);
    producto.setPrecioLista(new BigDecimal("100"));
    productos.add(producto);
    renglonesPedido.add(renglonPedido);
    when(renglonPedidoRepository.findByIdPedidoOrderByIdProductoItem(1L))
        .thenReturn(renglonesPedido);
    List<Long> idsProductos = new ArrayList<>();
    idsProductos.add(1L);
    when(productoService.getMultiplesProductosPorId(idsProductos)).thenReturn(productos);
    when(cuentaCorrienteService.getSaldoCuentaCorriente(1L)).thenReturn(new BigDecimal("900"));
    assertEquals(
        new BigDecimal("900.00000000000000000"),
        pedidoService.calcularTotalActualDePedido(pedido).getTotalActual());
    productos.get(0).setOferta(false);
    when(productoService.getMultiplesProductosPorId(idsProductos)).thenReturn(productos);
    assertEquals(
        new BigDecimal("990.00000000000000000"),
        pedidoService.calcularTotalActualDePedido(pedido).getTotalActual());
    productos.get(0).setOferta(false);
    productos.get(0).setPorcentajeBonificacionPrecio(null);
    when(productoService.getMultiplesProductosPorId(idsProductos)).thenReturn(productos);
    assertEquals(
        new BigDecimal("1000.00"),
        pedidoService.calcularTotalActualDePedido(pedido).getTotalActual());
  }

  @Test
  void shouldGetArrayDeIdProducto() {
    List<NuevoRenglonPedidoDTO> nuevosRenglonesPedido = new ArrayList<>();
    NuevoRenglonPedidoDTO nuevoRenglonPedidoDTO1 = new NuevoRenglonPedidoDTO();
    nuevoRenglonPedidoDTO1.setIdProductoItem(1L);
    NuevoRenglonPedidoDTO nuevoRenglonPedidoDTO2 = new NuevoRenglonPedidoDTO();
    nuevoRenglonPedidoDTO2.setIdProductoItem(5L);
    nuevosRenglonesPedido.add(nuevoRenglonPedidoDTO1);
    nuevosRenglonesPedido.add(nuevoRenglonPedidoDTO2);
    long[] idsProductoEsperado = new long[] {1L, 5L};
    long[] idsProductoResultado = pedidoService.getArrayDeIdProducto(nuevosRenglonesPedido);
    assertEquals(idsProductoEsperado.length, idsProductoResultado.length);
    assertEquals(idsProductoEsperado[0], idsProductoResultado[0]);
    assertEquals(idsProductoEsperado[1], idsProductoResultado[1]);
  }

  @Test
  void shouldGetArrayDeCantidadesProducto() {
    List<NuevoRenglonPedidoDTO> nuevosRenglonesPedido = new ArrayList<>();
    NuevoRenglonPedidoDTO nuevoRenglonPedidoDTO1 = new NuevoRenglonPedidoDTO();
    nuevoRenglonPedidoDTO1.setCantidad(BigDecimal.TEN);
    NuevoRenglonPedidoDTO nuevoRenglonPedidoDTO2 = new NuevoRenglonPedidoDTO();
    nuevoRenglonPedidoDTO2.setCantidad(BigDecimal.ONE);
    nuevosRenglonesPedido.add(nuevoRenglonPedidoDTO1);
    nuevosRenglonesPedido.add(nuevoRenglonPedidoDTO2);
    BigDecimal[] idsProductoEsperado = new BigDecimal[] {BigDecimal.TEN, BigDecimal.ONE};
    BigDecimal[] idsProductoResultado =
        pedidoService.getArrayDeCantidadesProducto(nuevosRenglonesPedido);
    assertEquals(idsProductoEsperado.length, idsProductoResultado.length);
    assertEquals(idsProductoEsperado[0], idsProductoResultado[0]);
    assertEquals(idsProductoEsperado[1], idsProductoResultado[1]);
  }
}
