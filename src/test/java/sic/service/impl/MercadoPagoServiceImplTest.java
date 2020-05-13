package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.*;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.*;
import sic.util.CustomValidator;
import sic.util.EncryptUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      MercadoPagoServiceImpl.class,
      EncryptUtils.class,
      MessageSource.class,
      CustomValidator.class
    })
@TestPropertySource(locations = "classpath:application.properties")
class MercadoPagoServiceImplTest {

  @MockBean IReciboService reciboService;
  @MockBean IClienteService clienteService;
  @MockBean INotaService notaService;
  @MockBean ISucursalService sucursalService;
  @MockBean ICarritoCompraService carritoCompraService;
  @MockBean IUsuarioService usuarioService;
  @MockBean IPedidoService pedidoService;
  @MockBean IProductoService productoService;
  @MockBean ICorreoElectronicoService correoElectronicoService;
  @MockBean MessageSource messageSource;

  @Autowired EncryptUtils encryptUtils;
  @Autowired MercadoPagoServiceImpl mercadoPagoService;

  @Test
  void shouldCrearNuevaPreference() {
    Cliente cliente = new Cliente();
    cliente.setEmail("test@test.com");
    cliente.setNroCliente("1234");
    cliente.setNombreFiscal("Jhon Test");
    Usuario usuario = new Usuario();
    usuario.setUsername("test");
    usuario.setNombre("Jhon Test");
    cliente.setCredencial(usuario);
    when(clienteService.getClientePorIdUsuario(anyLong())).thenReturn(cliente);
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("9 de Julio");
    when(sucursalService.getSucursalPorId(anyLong())).thenReturn(sucursal);
    when(usuarioService.getUsuarioNoEliminadoPorId(2L)).thenReturn(usuario);
    ItemCarritoCompra itemCarritoCompra = new ItemCarritoCompra();
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    itemCarritoCompra.setProducto(producto);
    itemCarritoCompra.setCantidad(BigDecimal.ONE);
    List<ItemCarritoCompra> itemCarritoCompras = Collections.singletonList(itemCarritoCompra);
    when(carritoCompraService.getItemsDelCarritoPorUsuario(usuario)).thenReturn(itemCarritoCompras);
    RenglonPedido renglonPedido = new RenglonPedido();
    when(pedidoService.calcularRenglonPedido(anyLong(), any())).thenReturn(renglonPedido);
    NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO =
        NuevaOrdenDePagoDTO.builder()
            .movimiento(Movimiento.DEPOSITO)
            .idSucursal(1L)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .monto(BigDecimal.TEN)
            .build();
    MercadoPagoPreferenceDTO mercadoPagoPreferenceDTO =
        mercadoPagoService.crearNuevaPreference(1L, nuevaOrdenDePagoDTO, "localhost");
    assertNotNull(mercadoPagoPreferenceDTO);
    assertNotNull(mercadoPagoPreferenceDTO.getId());
    assertNotEquals("", mercadoPagoPreferenceDTO.getId());
    assertNotNull(mercadoPagoPreferenceDTO.getInitPoint());
    assertNotEquals("", mercadoPagoPreferenceDTO.getInitPoint());
  }

  @Test
  void shouldCrearComprobantePorNotificacion() {
    Cliente cliente = new Cliente();
    cliente.setEmail("test@test.com");
    cliente.setNroCliente("1234");
    cliente.setNombreFiscal("Jhon Test");
    Usuario usuario = new Usuario();
    usuario.setUsername("test");
    usuario.setNombre("Jhon Test");
    cliente.setCredencial(usuario);
    when(clienteService.getClientePorIdUsuario(anyLong())).thenReturn(cliente);
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("9 de Julio");
    when(sucursalService.getSucursalPorId(anyLong())).thenReturn(sucursal);
    when(usuarioService.getUsuarioNoEliminadoPorId(2L)).thenReturn(usuario);
    ItemCarritoCompra itemCarritoCompra = new ItemCarritoCompra();
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    itemCarritoCompra.setProducto(producto);
    itemCarritoCompra.setCantidad(BigDecimal.ONE);
    List<ItemCarritoCompra> itemCarritoCompras = Collections.singletonList(itemCarritoCompra);
    when(carritoCompraService.getItemsDelCarritoPorUsuario(usuario)).thenReturn(itemCarritoCompras);
    RenglonPedido renglonPedido = new RenglonPedido();
    when(pedidoService.calcularRenglonPedido(anyLong(), any())).thenReturn(renglonPedido);
    Pedido pedido = new Pedido();
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    pedido.setSucursal(sucursal);
    pedido.setUsuario(usuario);
    pedido.setCliente(cliente);
    pedido.setTipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL);
    pedido.setIdPayment("24464889");
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    when(pedidoService.guardar(pedido, null)).thenReturn(pedido);
    mercadoPagoService.crearComprobantePorNotificacion("24464889");
    verify(reciboService, times(1)).getReciboPorIdMercadoPago(anyString());
    verify(clienteService, times(1)).getClientePorIdUsuario(anyLong());
    verify(sucursalService, times(1)).getSucursalPorId(any());
    verify(pedidoService, times(1)).getPedidoPorIdPayment(anyString());
    verify(messageSource, times(1)).getMessage(eq("mensaje_pago_aprobado"), any(), any());
    verify(usuarioService, times(1)).getUsuarioNoEliminadoPorId(anyLong());
    verify(carritoCompraService, times(1)).getItemsDelCarritoPorUsuario(any());
    verify(pedidoService, times(1)).calcularRenglonPedido(anyLong(), any());
    verify(pedidoService, times(1)).guardar(any(), any());
    verify(carritoCompraService).eliminarTodosLosItemsDelUsuario(anyLong());
  }
}
