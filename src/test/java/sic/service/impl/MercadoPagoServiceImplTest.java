package sic.service.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.service.*;
import sic.util.CustomValidator;
import sic.util.EncryptUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    ItemCarritoCompra itemCarritoCompra1 = new ItemCarritoCompra();
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    itemCarritoCompra1.setProducto(producto1);
    itemCarritoCompra1.setCantidad(BigDecimal.ONE);
    ItemCarritoCompra itemCarritoCompra2 = new ItemCarritoCompra();
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    itemCarritoCompra2.setProducto(producto2);
    itemCarritoCompra2.setCantidad(BigDecimal.TEN);
    List<ItemCarritoCompra> itemCarritoCompras = new ArrayList<>();
    itemCarritoCompras.add(itemCarritoCompra1);
    itemCarritoCompras.add(itemCarritoCompra2);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
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
    long[] idProducto = new long[]{1L, 2L};
    BigDecimal[] cantidad = new BigDecimal[]{BigDecimal.ONE, BigDecimal.TEN};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
            ProductosParaVerificarStockDTO.builder().idProducto(idProducto).cantidad(cantidad).build();
    verify(productoService).getProductosSinStockDisponible(productosParaVerificarStockDTO);
    assertNotNull(mercadoPagoPreferenceDTO);
    assertNotNull(mercadoPagoPreferenceDTO.getId());
    assertNotEquals("", mercadoPagoPreferenceDTO.getId());
    assertNotNull(mercadoPagoPreferenceDTO.getInitPoint());
    assertNotEquals("", mercadoPagoPreferenceDTO.getInitPoint());
    NuevaOrdenDePagoDTO ordenDePagoSinIdSucursalConRetiroEnLaMisma =
        NuevaOrdenDePagoDTO.builder()
            .movimiento(Movimiento.DEPOSITO)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .monto(BigDecimal.TEN)
            .build();
    assertThrows(
        BusinessServiceException.class,
        () ->
            mercadoPagoService.crearNuevaPreference(
                1L, ordenDePagoSinIdSucursalConRetiroEnLaMisma, "localhost"));
    verify(messageSource)
        .getMessage(eq("mensaje_preference_retiro_sucursal_no_seleccionada"), any(), any());
    NuevaOrdenDePagoDTO ordenDePagoConUbicacionEnvio =
        NuevaOrdenDePagoDTO.builder()
            .movimiento(Movimiento.DEPOSITO)
            .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_ENVIO)
            .monto(BigDecimal.TEN)
            .build();
    when(sucursalService.getSucursalPredeterminada()).thenReturn(sucursal);
    cliente.setEmail(null);
    when(clienteService.getClientePorIdUsuario(2L)).thenReturn(cliente);
    assertThrows(
        BusinessServiceException.class,
        () ->
            mercadoPagoService.crearNuevaPreference(2L, ordenDePagoConUbicacionEnvio, "localhost"));
    verify(messageSource).getMessage(eq("mensaje_preference_cliente_sin_email"), any(), any());
    NuevaOrdenDePagoDTO ordenDePagoPedido =
        NuevaOrdenDePagoDTO.builder()
            .movimiento(Movimiento.PEDIDO)
            .idSucursal(1L)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .monto(BigDecimal.TEN)
            .build();
    cliente.setEmail("test@test.com");
    when(clienteService.getClientePorIdUsuario(anyLong())).thenReturn(cliente);
    Pedido pedido = new Pedido();
    pedido.setFecha(LocalDateTime.now());
    pedido.setFechaVencimiento(LocalDateTime.now());
    when(pedidoService.guardar(any(), any())).thenReturn(pedido);
    when(carritoCompraService.calcularTotal(1L)).thenReturn(new BigDecimal("1000.00"));
    MercadoPagoPreferenceDTO mercadoPagoPreference =
        mercadoPagoService.crearNuevaPreference(1L, ordenDePagoPedido, "localhost");
    assertNotNull(mercadoPagoPreference.getId());
    assertNotNull(mercadoPagoPreference.getInitPoint());
    assertEquals('-', mercadoPagoPreference.getId().charAt(9));
    assertTrue(mercadoPagoPreference.getInitPoint().startsWith("https://www.mercadopago.com.ar/"));
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
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    when(pedidoService.guardar(pedido, null)).thenReturn(pedido);
    mercadoPagoService.crearComprobantePorNotificacion("26800675");
    verify(reciboService, times(1)).getReciboPorIdMercadoPago(anyString());
    verify(clienteService, times(1)).getClientePorIdUsuario(anyLong());
    verify(sucursalService, times(1)).getSucursalPorId(any());
    verify(messageSource, times(1)).getMessage(eq("mensaje_pago_aprobado"), any(), any());
  }
}
