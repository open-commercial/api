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
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.repository.PedidoRepository;
import sic.repository.RenglonPedidoRepository;
import sic.util.CustomValidator;
import sic.util.JasperReportsHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                PedidoServiceImpl.class,
                CustomValidator.class,
                MessageSource.class,
                JasperReportsHandler.class
        })
class PedidoServiceImplTest {

  @MockBean PedidoRepository pedidoRepository;
  @MockBean RenglonPedidoRepository renglonPedidoRepository;
  @MockBean FacturaVentaServiceImpl facturaVentaService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean EmailServiceFactory emailServiceFactory;
  @MockBean ResendEmailServiceImpl resendEmailService;
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
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    when(pedidoRepository.save(pedido)).thenReturn(pedido);
    pedidoService.cancelar(pedido);
    verify(pedidoRepository).save(pedido);
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
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCodigo("123");
    producto.setDescripcion("desc producto");
    producto.setUrlImagen("url");
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto);
    Sucursal sucursal = new Sucursal();
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursal.setCantidad(BigDecimal.ONE);
    cantidadEnSucursales.add(cantidadEnSucursal);
    producto.setCantidadProducto(new CantidadProductoEmbeddable());
    producto.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setImporte(new BigDecimal("1000"));
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    cliente.setEmail("email@cliente.com");
    pedido.setCliente(cliente);
    cliente.setPuedeComprarAPlazo(true);
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    when(pedidoRepository.save(pedido)).thenReturn(pedido);
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    pedido.setEstado(EstadoPedido.ABIERTO);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setVencimientoLargo(15L);
    configuracionSucursal.setVencimientoCorto(1L);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    pedido.setSucursal(sucursal);
    Usuario usuarioPedido = new Usuario();
    List<Rol> roles = new ArrayList<>();
    roles.add(Rol.VENDEDOR);
    usuarioPedido.setIdUsuario(1L);
    usuarioPedido.setRoles(roles);
    pedido.setUsuario(usuarioPedido);
    pedido.setDescuentoPorcentaje(BigDecimal.ONE);
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(cliente);
    assertThrows(BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(
            eq("mensaje_no_se_puede_guardar_pedido_con_descuento_usuario_cliente_iguales"), any(), any());
    Cliente clienteDeUsuario = new Cliente();
    clienteDeUsuario.setIdCliente(2L);
    clienteDeUsuario.setNombreFiscal("nombre fiscal");
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(clienteDeUsuario);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    assertThrows(BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaje_pedido_detalle_envio_vacio"), any(), any());
    UbicacionDTO ubicacionDTO = new UbicacionDTO();
    pedido.setDetalleEnvio(ubicacionDTO);
    List<ProductoFaltanteDTO> faltantes = new ArrayList<>();
    ProductoFaltanteDTO productoFaltante = new ProductoFaltanteDTO();
    productoFaltante.setIdProducto(1L);
    faltantes.add(productoFaltante);
    when(productoService.getProductosSinStockDisponible(any())).thenReturn(faltantes);
    assertThrows(BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaje_pedido_sin_stock"), any(), any());
    when(productoService.getProductosSinStockDisponible(any())).thenReturn(new ArrayList<>());
    when(emailServiceFactory.getEmailService(anyString())).thenReturn(resendEmailService);
    Pedido pedidoGuardado = pedidoService.guardar(pedido, new ArrayList<>());
    assertNotNull(pedidoGuardado);
    assertEquals(1, pedidoGuardado.getRenglones().size());
    assertEquals(new BigDecimal("1000.000000000000000"), pedidoGuardado.getTotal());
    assertEquals(EstadoPedido.ABIERTO, pedidoGuardado.getEstado());
    cliente.setPuedeComprarAPlazo(false);
    pedido.setCliente(cliente);
    List<Recibo> recibos = new ArrayList<>();
    Recibo recibo = new Recibo();
    recibo.setMonto(new BigDecimal("1000"));
    recibos.add(recibo);
    when(cuentaCorrienteService.getSaldoCuentaCorriente(1L)).thenReturn(new BigDecimal("-1000"));
    assertThrows(BusinessServiceException.class, () -> pedidoService.guardar(pedido, recibos));
    verify(messageSource).getMessage(eq("mensaje_cliente_no_puede_comprar_a_plazo"), any(), any());
    when(cuentaCorrienteService.getSaldoCuentaCorriente(1L)).thenReturn(new BigDecimal("-1000"));
    assertThrows(BusinessServiceException.class, () -> pedidoService.guardar(pedido, new ArrayList<>()));
    verify(messageSource).getMessage(eq("mensaje_cliente_saldar_cc"), any(), any());
    when(cuentaCorrienteService.getSaldoCuentaCorriente(1L)).thenReturn(new BigDecimal("0"));
    pedidoGuardado = pedidoService.guardar(pedido, recibos);
    assertEquals(pedidoGuardado.getFecha().plusMinutes(15L).truncatedTo(ChronoUnit.MINUTES),
            pedido.getFechaVencimiento().truncatedTo(ChronoUnit.MINUTES));
    pedidoGuardado = pedidoService.guardar(pedido, new ArrayList<>( ));
    assertEquals(pedidoGuardado.getFecha().plusMinutes(1L).truncatedTo(ChronoUnit.MINUTES),
            pedido.getFechaVencimiento().truncatedTo(ChronoUnit.MINUTES));
    verify(reciboService).guardar(any());
  }

  @Test
  void shouldValidarReglasDeNegocio() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CERRADO);
    pedido.setNroPedido(123L);
    Sucursal sucursal = new Sucursal();
    pedido.setSucursal(sucursal);
    assertThrows(
        BusinessServiceException.class,
        () -> pedidoService.validarReglasDeNegocio(TipoDeOperacion.ALTA, pedido));
    verify(messageSource).getMessage(eq("mensaja_estado_no_valido"), any(), any());
    pedido.setEstado(EstadoPedido.ABIERTO);
    when(pedidoRepository.existsByNroPedidoAndSucursal(123L, sucursal))
        .thenReturn(true);
    assertThrows(
        BusinessServiceException.class,
        () -> pedidoService.validarReglasDeNegocio(TipoDeOperacion.ALTA, pedido));
    when(pedidoRepository.existsByNroPedidoAndSucursal(123L, sucursal))
        .thenReturn(false);
    assertThrows(
        BusinessServiceException.class,
        () -> pedidoService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, pedido));
    verify(messageSource).getMessage(eq("mensaje_pedido_duplicado"), any(), any());
    verify(messageSource).getMessage(eq("mensaje_pedido_no_existente"), any(), any());
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setImporte(new BigDecimal("1000"));
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    List<ProductoFaltanteDTO> faltantes = new ArrayList<>();
    ProductoFaltanteDTO faltante = new ProductoFaltanteDTO();
    faltante.setIdProducto(1L);
    faltantes.add(faltante);
    when(productoService.getProductosSinStockDisponible(any())).thenReturn(faltantes);
    assertThrows(
        BusinessServiceException.class,
        () -> pedidoService.validarReglasDeNegocio(TipoDeOperacion.ALTA, pedido));
    verify(messageSource).getMessage(eq("mensaje_pedido_detalle_envio_vacio"), any(), any());
    pedido.setDetalleEnvio(new UbicacionDTO());
    assertThrows(
        BusinessServiceException.class,
        () -> pedidoService.validarReglasDeNegocio(TipoDeOperacion.ALTA, pedido));
    verify(messageSource).getMessage(eq("mensaje_pedido_sin_stock"), any(), any());
  }

  @Test
  void shouldCambiarFechaDeVencimiento() {
    Pedido pedido = new Pedido();
    pedido.setFecha(LocalDateTime.now());
    when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setVencimientoLargo(1L);
    configuracionSucursal.setVencimientoCorto(1L);
    Sucursal sucursal = new Sucursal();
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    pedido.setSucursal(sucursal);
    pedidoService.cambiarFechaDeVencimiento(1L);
    verify(pedidoRepository).save(pedido);
  }

  @Test
  void shouldTestBusquedaDePedidos() {
    BusquedaPedidoCriteria busquedaReciboCriteria =
        BusquedaPedidoCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MIN)
            .idCliente(2L)
            .idUsuario(3L)
            .idViajante(5L)
            .nroPedido(123L)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .idProducto(1L)
            .idSucursal(7L)
            .build();
    Usuario usuario = new Usuario();
    List<Rol> roles = new ArrayList<>();
    roles.add(Rol.COMPRADOR);
    usuario.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    String builder =
        "pedido.sucursal.idSucursal = 7 && pedido.fecha between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999 "
            + "&& pedido.cliente.idCliente = 2 && pedido.usuario.idUsuario = 3 "
            + "&& pedido.cliente.viajante.idUsuario = 5 && pedido.nroPedido = 123 "
            + "&& pedido.tipoDeEnvio = RETIRO_EN_SUCURSAL && any(pedido.renglones).idProductoItem = 1 && pedido.eliminado = false";
    assertEquals(builder, pedidoService.getBuilderPedido(busquedaReciboCriteria, 1L).toString());
    busquedaReciboCriteria =
        BusquedaPedidoCriteria.builder()
            .fechaDesde(LocalDateTime.MIN)
            .idCliente(2L)
            .idUsuario(3L)
            .idViajante(5L)
            .nroPedido(123L)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .idProducto(1L)
            .idSucursal(7L)
            .build();
    builder =
        "pedido.sucursal.idSucursal = 7 && pedido.fecha > -999999999-01-01T00:00 " +
                "&& pedido.cliente.idCliente = 2 && pedido.usuario.idUsuario = 3 " +
                "&& pedido.cliente.viajante.idUsuario = 5 && pedido.nroPedido = 123 " +
                "&& pedido.tipoDeEnvio = RETIRO_EN_SUCURSAL && any(pedido.renglones).idProductoItem = 1 " +
                "&& pedido.eliminado = false";
    assertEquals(builder, pedidoService.getBuilderPedido(busquedaReciboCriteria, 1L).toString());
    busquedaReciboCriteria =
        BusquedaPedidoCriteria.builder()
            .fechaHasta(LocalDateTime.MIN)
            .idCliente(2L)
            .idUsuario(3L)
            .idViajante(5L)
            .nroPedido(123L)
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .idProducto(1L)
            .idSucursal(7L)
            .build();
    builder =
        "pedido.sucursal.idSucursal = 7 && pedido.fecha < -999999999-01-01T23:59:59.999999999 " +
                "&& pedido.cliente.idCliente = 2 && pedido.usuario.idUsuario = 3 " +
                "&& pedido.cliente.viajante.idUsuario = 5 && pedido.nroPedido = 123 " +
                "&& pedido.tipoDeEnvio = RETIRO_EN_SUCURSAL && any(pedido.renglones).idProductoItem = 1 && pedido.eliminado = false";
    assertEquals(builder, pedidoService.getBuilderPedido(busquedaReciboCriteria, 1L).toString());
  }

  @Test
  void shouldActualizarCantidadReservadaDeProductosPorAlta() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    pedidoService.actualizarCantidadReservadaDeProductosPorCambioDeEstado(pedido);
    verify(productoService).agregarCantidadReservada(1L, BigDecimal.TEN);
  }

  @Test
  void shouldActualizarCantidadReservadaDeProductosPorCancelacion() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CANCELADO);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    pedidoService.actualizarCantidadReservadaDeProductosPorCambioDeEstado(pedido);
    verify(productoService).quitarCantidadReservada(1L, BigDecimal.TEN);
  }

  @Test
  void shouldActualizarCantidadReservadaDeProductosPorCierre() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CERRADO);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    pedidoService.actualizarCantidadReservadaDeProductosPorCambioDeEstado(pedido);
    verify(productoService).quitarCantidadReservada(1L, BigDecimal.TEN);
  }

  @Test
  void shouldActualizarCantidadReservadaDeProductosPorModificacion() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglonesPedido.add(renglonPedido);
    pedido.setRenglones(renglonesPedido);
    List<RenglonPedido> renglonesAnterioresPedido = new ArrayList<>();
    RenglonPedido renglonAnteriorPedido = new RenglonPedido();
    renglonAnteriorPedido.setIdProductoItem(5L);
    renglonAnteriorPedido.setCantidad(BigDecimal.ONE);
    renglonesAnterioresPedido.add(renglonAnteriorPedido);
    pedidoService.actualizarCantidadReservadaDeProductosPorModificacion(pedido, renglonesAnterioresPedido);
    verify(productoService).quitarCantidadReservada(5L, BigDecimal.ONE);
    verify(productoService).agregarCantidadReservada(1L, BigDecimal.TEN);
  }

  @Test
  void shouldThrowsServiceExceptionActualizarCantidadReservadaDeProductosPorModificacion() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.CANCELADO);
    List<RenglonPedido> renglonesAnterioresPedido = new ArrayList<>();
    RenglonPedido renglonAnteriorPedido = new RenglonPedido();
    renglonAnteriorPedido.setIdProductoItem(5L);
    renglonAnteriorPedido.setCantidad(BigDecimal.ONE);
    renglonesAnterioresPedido.add(renglonAnteriorPedido);
    assertThrows(
            ServiceException.class,
            () -> pedidoService.actualizarCantidadReservadaDeProductosPorModificacion(pedido, renglonesAnterioresPedido));
    verify(messageSource)
            .getMessage(eq("mensaje_producto_error_actualizar_cantidad_reservada"), any(), any());
  }
}
