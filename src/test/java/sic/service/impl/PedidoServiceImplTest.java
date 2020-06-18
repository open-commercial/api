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
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.PedidoRepository;
import sic.repository.RenglonPedidoRepository;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
  @MockBean ReciboServiceImpl reciboService;
  @MockBean MessageSource messageSource;
  @MockBean ModelMapper modelMapper;

  @Autowired PedidoServiceImpl pedidoService;

  @Test
  void shouldCancelarPedidoAbierto() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    pedidoService.cancelar(pedido);
    verify(pedidoRepository, times(1)).save(pedido);
  }

  @Test
  void shouldEliminarPedidoAbierto() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    pedidoService.eliminar(1L);
    verify(pedidoRepository, times(1)).delete(pedido);
  }

  @Test
  void shouldNotCancelarPedidoCerrado() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CERRADO);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    assertThrows(BusinessServiceException.class, () -> pedidoService.cancelar(pedido));
    verify(messageSource).getMessage(eq("mensaje_no_se_puede_cancelar_pedido"), any(), any());
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

  @Test
  void shouldGuardarPedido() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CANCELADO);
    pedido.setIdPedido(1L);
    Sucursal sucursal = new Sucursal();
    pedido.setSucursal(sucursal);
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCodigo("123");
    producto.setDescripcion("desc producto");
    producto.setUrlImagen("url");
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto);
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursal.setCantidad(BigDecimal.ONE);
    cantidadEnSucursales.add(cantidadEnSucursal);
    producto.setCantidadEnSucursales(cantidadEnSucursales);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setImporte(new BigDecimal("1000"));
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    Cliente cliente = new Cliente();
    cliente.setPuedeComprarAPlazo(false);
    cliente.setEmail("email@cliente.com");
    pedido.setCliente(cliente);
    assertThrows(
        BusinessServiceException.class,
        () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaje_cliente_no_puede_comprar_a_plazo"), any(), any());
    cliente.setPuedeComprarAPlazo(true);
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    when(pedidoRepository.save(pedido)).thenReturn(pedido);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    pedido.setEstado(EstadoPedido.CANCELADO);
    assertThrows(
        BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaja_estado_no_valido"), any(), any());
    pedido.setEstado(EstadoPedido.ABIERTO);
    assertThrows(
            BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaje_pedido_detalle_envio_vacio"), any(), any());
    UbicacionDTO ubicacionDTO = new UbicacionDTO();
    pedido.setDetalleEnvio(ubicacionDTO);
    List<ProductoFaltanteDTO> faltantes = new ArrayList<>();
    ProductoFaltanteDTO productoFaltante = new ProductoFaltanteDTO();
    productoFaltante.setIdProducto(1L);
    faltantes.add(productoFaltante);
    when(productoService.getProductosSinStockDisponible(any())).thenReturn(faltantes);
    assertThrows(
            BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaje_pedido_sin_stock"), any(), any());
    when(productoService.getProductosSinStockDisponible(any())).thenReturn(new ArrayList<>());
    Pedido pedidoGuardado = pedidoService.guardar(pedido, new ArrayList<>());
    assertNotNull(pedidoGuardado);
    assertEquals(1, pedidoGuardado.getRenglones().size());
    assertEquals(new BigDecimal("100"), pedidoGuardado.getTotalActual());
    assertEquals(EstadoPedido.ABIERTO, pedidoGuardado.getEstado());
  }
}
