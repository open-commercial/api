package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.NuevaFacturaVentaDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.FacturaRepository;
import sic.repository.FacturaVentaRepository;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      CustomValidator.class,
      FacturaVentaServiceImpl.class,
      FacturaServiceImpl.class,
      MessageSource.class
    })
class FacturaVentaServiceImplTest {

  @MockBean FacturaRepository<Factura> facturaRepository;
  @MockBean FacturaVentaRepository facturaVentaRepository;
  @MockBean ProductoServiceImpl productoService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean PedidoServiceImpl pedidoService;
  @MockBean ConfiguracionSucursalServiceImpl configuracionSucursalService;
  @MockBean CorreoElectronicoServiceImpl correoElectronicoService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean TransportistaServiceImpl transportistaService;
  @MockBean MessageSource messageSource;

  @Autowired FacturaServiceImpl facturaServiceImpl;
  @Autowired FacturaVentaServiceImpl facturaVentaServiceImpl;

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalDiscriminaYClienteTambien() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Usuario usuario = new Usuario();
    usuario.setRoles(Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO));
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(1L, 1L, 1L);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalDiscriminaYClienteNo() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Usuario usuario = new Usuario();
    usuario.setRoles(Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO));
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(1L, 1L, 1L);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalNoDiscriminaYClienteSi() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO);
    Usuario usuario = new Usuario();
    usuario.setRoles(Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO));
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(1L, 1L, 1L);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldGetTipoFacturaVentaWhenSucursalNoDiscriminaIVAYClienteNO() {
    Sucursal sucursal = new Sucursal();
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Cliente cliente = new Cliente();
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    Usuario usuario = new Usuario();
    usuario.setRoles(Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO));
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    TipoDeComprobante[] expResult = {
      TipoDeComprobante.FACTURA_C, TipoDeComprobante.FACTURA_X, TipoDeComprobante.PRESUPUESTO
    };
    TipoDeComprobante[] result = facturaVentaServiceImpl.getTiposDeComprobanteVenta(1L, 1L, 1L);
    assertArrayEquals(expResult, result);
  }

  @Test
  void shouldDividirFactura() {
    when(facturaVentaRepository.buscarMayorNumFacturaSegunTipo(TipoDeComprobante.FACTURA_X, 1L, 1L))
        .thenReturn(1L);
    when(facturaVentaRepository.buscarMayorNumFacturaSegunTipo(TipoDeComprobante.FACTURA_A, 1L, 1L))
        .thenReturn(1L);
    RenglonFactura renglon1 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon2 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon3 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon4 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon5 = Mockito.mock(RenglonFactura.class);
    RenglonFactura renglon6 = Mockito.mock(RenglonFactura.class);
    Producto producto1 = Mockito.mock(Producto.class);
    Producto producto2 = Mockito.mock(Producto.class);
    Producto producto3 = Mockito.mock(Producto.class);
    Producto producto4 = Mockito.mock(Producto.class);
    Producto producto5 = Mockito.mock(Producto.class);
    Producto producto6 = Mockito.mock(Producto.class);
    Medida medida = Mockito.mock(Medida.class);
    when(producto1.getIdProducto()).thenReturn(1L);
    when(producto1.getCodigo()).thenReturn("1");
    when(producto1.getDescripcion()).thenReturn("producto uno test");
    when(producto1.getMedida()).thenReturn(medida);
    when(producto1.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto1.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto1.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto1.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto1);
    when(producto2.getIdProducto()).thenReturn(2L);
    when(producto2.getCodigo()).thenReturn("2");
    when(producto2.getDescripcion()).thenReturn("producto dos test");
    when(producto2.getMedida()).thenReturn(medida);
    when(producto2.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto2.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto2.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto2.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(2L)).thenReturn(producto2);
    when(producto3.getIdProducto()).thenReturn(3L);
    when(producto3.getCodigo()).thenReturn("3");
    when(producto3.getDescripcion()).thenReturn("producto tres test");
    when(producto3.getMedida()).thenReturn(medida);
    when(producto3.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto3.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto3.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto3.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(3L)).thenReturn(producto3);
    when(producto4.getIdProducto()).thenReturn(4L);
    when(producto4.getCodigo()).thenReturn("4");
    when(producto4.getDescripcion()).thenReturn("producto cuatro test");
    when(producto4.getMedida()).thenReturn(medida);
    when(producto4.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto4.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto4.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto4.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(4L)).thenReturn(producto4);
    when(producto5.getIdProducto()).thenReturn(5L);
    when(producto5.getCodigo()).thenReturn("5");
    when(producto5.getDescripcion()).thenReturn("producto cinco test");
    when(producto5.getMedida()).thenReturn(medida);
    when(producto5.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto5.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto5.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto5.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(5L)).thenReturn(producto5);
    when(producto6.getIdProducto()).thenReturn(6L);
    when(producto6.getCodigo()).thenReturn("6");
    when(producto6.getDescripcion()).thenReturn("producto seis test");
    when(producto6.getMedida()).thenReturn(medida);
    when(producto6.getBulto()).thenReturn(BigDecimal.ONE);
    when(producto6.getPrecioVentaPublico()).thenReturn(BigDecimal.ONE);
    when(producto6.getIvaPorcentaje()).thenReturn(new BigDecimal("21.00"));
    when(producto6.getPrecioLista()).thenReturn(BigDecimal.ONE);
    when(productoService.getProductoNoEliminadoPorId(6L)).thenReturn(producto6);
    when(renglon1.getIdProductoItem()).thenReturn(1L);
    when(renglon1.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon1.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon1.getCantidad()).thenReturn(new BigDecimal("4.00"));
    when(renglon2.getIdProductoItem()).thenReturn(2L);
    when(renglon2.getIvaNeto()).thenReturn(new BigDecimal("10.5"));
    when(renglon2.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon2.getCantidad()).thenReturn(new BigDecimal("7.00"));
    when(renglon3.getIdProductoItem()).thenReturn(3L);
    when(renglon3.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon3.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon3.getCantidad()).thenReturn(new BigDecimal("12.8"));
    when(renglon4.getIdProductoItem()).thenReturn(4L);
    when(renglon4.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon4.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon4.getCantidad()).thenReturn(new BigDecimal("1.2"));
    when(renglon5.getIdProductoItem()).thenReturn(5L);
    when(renglon5.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon5.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon5.getCantidad()).thenReturn(new BigDecimal("0.8"));
    when(renglon6.getIdProductoItem()).thenReturn(6L);
    when(renglon6.getIvaNeto()).thenReturn(new BigDecimal("21"));
    when(renglon6.getBonificacionPorcentaje()).thenReturn(BigDecimal.ZERO);
    when(renglon6.getCantidad()).thenReturn(new BigDecimal("9.3"));
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglon1);
    renglones.add(renglon2);
    renglones.add(renglon3);
    renglones.add(renglon6); // no participa de la division
    renglones.add(renglon4);
    renglones.add(renglon5); // no participa de la division
    FacturaVenta factura = new FacturaVenta();
    factura.setDescuentoPorcentaje(BigDecimal.ZERO);
    factura.setRecargoPorcentaje(BigDecimal.ZERO);
    factura.setRenglones(renglones);
    factura.setFecha(LocalDateTime.now());
    factura.setTransportista(new Transportista());
    factura.setSucursal(new Sucursal());
    Cliente cliente = new Cliente();
    cliente.setIdCliente(1L);
    factura.setCliente(cliente);
    Usuario usuario = new Usuario();
    usuario.setNombre("Marian Jhons  help");
    factura.setUsuario(usuario);
    factura.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    int[] indices = {0, 1, 2, 4};
    int cantidadDeFacturasEsperadas = 2;
    int cantidadDeRenglonesEsperadosFX = 4;
    int cantidadDeRenglonesEsperadosFA = 6;
    List<FacturaVenta> result = facturaVentaServiceImpl.dividirFactura(factura, indices);
    assertEquals(cantidadDeFacturasEsperadas, result.size());
    assertEquals(cantidadDeRenglonesEsperadosFX, result.get(0).getRenglones().size());
    assertEquals(cantidadDeRenglonesEsperadosFA, result.get(1).getRenglones().size());
    BigDecimal cantidadPrimerRenglonFacturaX = result.get(0).getRenglones().get(0).getCantidad();
    BigDecimal cantidadSegundoRenglonFacturaX = result.get(0).getRenglones().get(1).getCantidad();
    BigDecimal cantidadTercerRenglonFacturaX = result.get(0).getRenglones().get(2).getCantidad();
    BigDecimal cantidadCuartoRenglonFacturaX = result.get(0).getRenglones().get(3).getCantidad();
    BigDecimal cantidadPrimerRenglonFacturaA = result.get(1).getRenglones().get(0).getCantidad();
    BigDecimal cantidadSegundoRenglonFacturaA = result.get(1).getRenglones().get(1).getCantidad();
    BigDecimal cantidadTercerRenglonFacturaA = result.get(1).getRenglones().get(2).getCantidad();
    BigDecimal cantidadCuartoRenglonFacturaA = result.get(1).getRenglones().get(3).getCantidad();
    BigDecimal cantidadQuintoRenglonFacturaA = result.get(1).getRenglones().get(4).getCantidad();
    BigDecimal cantidadSextoRenglonFacturaA = result.get(1).getRenglones().get(5).getCantidad();
    assertEquals(0, cantidadPrimerRenglonFacturaA.compareTo(new BigDecimal("2")));
    assertEquals(0, cantidadSegundoRenglonFacturaA.compareTo(new BigDecimal("4")));
    assertEquals(0, cantidadTercerRenglonFacturaA.compareTo(new BigDecimal("6.4")));
    assertEquals(0, cantidadCuartoRenglonFacturaA.compareTo(new BigDecimal("9.3")));
    assertEquals(0, cantidadQuintoRenglonFacturaA.compareTo(new BigDecimal("0.6")));
    assertEquals(0, cantidadSextoRenglonFacturaA.compareTo(new BigDecimal("0.8")));
    assertEquals(0, cantidadPrimerRenglonFacturaX.compareTo(new BigDecimal("2")));
    assertEquals(0, cantidadSegundoRenglonFacturaX.compareTo(new BigDecimal("3")));
    assertEquals(0, cantidadTercerRenglonFacturaX.compareTo(new BigDecimal("6.4")));
    assertEquals(0, cantidadCuartoRenglonFacturaX.compareTo(new BigDecimal("0.6")));
  }

  @Test
  void shouldThrownBusinessServiceExceptionPorBusquedaVentaSinIdSucursal() {
    BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder().build();
    assertThrows(
        BusinessServiceException.class, () -> facturaVentaServiceImpl.getBuilderVenta(criteria));
    verify(messageSource).getMessage(eq("mensaje_busqueda_sin_sucursal"), any(), any());
  }

  @Test
  void shouldTestBusquedaFacturaVentaCriteria() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaDesde(LocalDateTime.MIN)
            .fechaHasta(LocalDateTime.MIN)
            .idCliente(1L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .nroPedido(33L)
            .serieRemito(3L)
            .nroRemito(4L)
            .build();
    String resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha between -999999999-01-01T00:00 and -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.remito.serie = 3 "
            + "&& facturaVenta.remito.nroRemito = 4 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria).toString());
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idCliente(1L)
            .idSucursal(1L)
            .fechaDesde(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha > -999999999-01-01T00:00 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria).toString());
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .idCliente(1L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha < -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria).toString());
    roles = Collections.singletonList(Rol.COMPRADOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    Cliente clienteRelacionadoConUsuarioLogueado = new Cliente();
    clienteRelacionadoConUsuarioLogueado.setIdCliente(6L);
    when(clienteService.getClientePorIdUsuario(1L))
        .thenReturn(clienteRelacionadoConUsuarioLogueado);
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .idCliente(1L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha < -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria).toString());
    when(clienteService.getClientePorIdUsuario(1L)).thenReturn(null);
    criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .fechaHasta(LocalDateTime.MIN)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .idProducto(3L)
            .numSerie(4L)
            .numFactura(5L)
            .idUsuario(7L)
            .idViajante(9L)
            .idCliente(1L)
            .nroPedido(33L)
            .build();
    resultadoBuilder =
        "facturaVenta.sucursal.idSucursal = 1 && facturaVenta.eliminada = false "
            + "&& facturaVenta.fecha < -999999999-01-01T23:59:59.999999999 "
            + "&& facturaVenta.cliente.idCliente = 1 && facturaVenta.tipoComprobante = FACTURA_A "
            + "&& facturaVenta.usuario.idUsuario = 7 && facturaVenta.cliente.viajante.idUsuario = 9 "
            + "&& facturaVenta.numSerie = 4 && facturaVenta.numFactura = 5 && facturaVenta.pedido.nroPedido = 33 "
            + "&& any(facturaVenta.renglones).idProductoItem = 3";
    assertEquals(resultadoBuilder, facturaVentaServiceImpl.getBuilderVenta(criteria).toString());
  }

  @Test
  void shouldCalcularTotalFacturadoVenta() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(facturaVentaRepository.calcularTotalFacturadoVenta(builder)).thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaVentaServiceImpl.calcularTotalFacturadoVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularTotalFacturadoVentaAndReturnZero() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(facturaVentaRepository.calcularTotalFacturadoVenta(builder)).thenReturn(null);
    assertEquals(
        BigDecimal.ZERO, facturaVentaServiceImpl.calcularTotalFacturadoVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularIvaVenta() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    when(facturaVentaRepository.calcularIVAVenta(builder, tipoFactura)).thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaVentaServiceImpl.calcularIvaVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularIvaVentaAndReturnZero() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    when(facturaVentaRepository.calcularIVAVenta(builder, tipoFactura)).thenReturn(null);
    assertEquals(BigDecimal.ZERO, facturaVentaServiceImpl.calcularIvaVenta(criteria, 1L));
  }

  @Test
  void shouldCalcularGananciaTotalVenta() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(facturaVentaRepository.calcularGananciaTotal(builder)).thenReturn(BigDecimal.TEN);
    assertEquals(BigDecimal.TEN, facturaVentaServiceImpl.calcularGananciaTotal(criteria, 1L));
  }

  @Test
  void shouldCalcularGananciaTotalVentaAndReturnZero() {
    Usuario usuarioLogueado = new Usuario();
    List<Rol> roles = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR);
    usuarioLogueado.setRoles(roles);
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuarioLogueado);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BooleanBuilder builder = new BooleanBuilder();
    QFacturaVenta qFacturVenta = QFacturaVenta.facturaVenta;
    builder.and(
        qFacturVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturVenta.eliminada.eq(false)));
    when(facturaVentaRepository.calcularGananciaTotal(builder)).thenReturn(null);
    assertEquals(BigDecimal.ZERO, facturaVentaServiceImpl.calcularGananciaTotal(criteria, 1L));
  }

  @Test
  void shouldTestRenglonesDelPedidoParaFacturar() {
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido1 = new RenglonPedido();
    renglonPedido1.setIdProductoItem(1L);
    renglonPedido1.setCantidad(new BigDecimal("10"));
    RenglonPedido renglonPedido2 = new RenglonPedido();
    renglonPedido2.setIdProductoItem(2L);
    renglonPedido2.setCantidad(new BigDecimal("20"));
    renglonesPedido.add(renglonPedido1);
    renglonesPedido.add(renglonPedido2);
    Map<Long, BigDecimal> renglonesDeFacturas = new HashMap<>();
    renglonesDeFacturas.put(1L, new BigDecimal("5"));
    renglonesDeFacturas.put(2L, new BigDecimal("15"));
    RenglonFactura renglonFactura1 = new RenglonFactura();
    renglonFactura1.setIdProductoItem(1L);
    renglonFactura1.setCantidad(new BigDecimal("5"));
    RenglonFactura renglonFactura2 = new RenglonFactura();
    renglonFactura2.setIdProductoItem(2L);
    renglonFactura2.setCantidad(new BigDecimal("5"));
    when(pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(1L)).thenReturn(renglonesPedido);
    Producto productoParaRetorno = new Producto();
    productoParaRetorno.setIdProducto(1L);
    productoParaRetorno.setCodigo("1");
    productoParaRetorno.setDescripcion("Producto para test");
    productoParaRetorno.setMedida(new Medida());
    productoParaRetorno.setPrecioCosto(new BigDecimal("89.35"));
    productoParaRetorno.setGananciaPorcentaje(new BigDecimal("38.74"));
    productoParaRetorno.setGananciaNeto(new BigDecimal("34.62"));
    productoParaRetorno.setPrecioVentaPublico(new BigDecimal("123.97"));
    productoParaRetorno.setIvaPorcentaje(new BigDecimal("21"));
    productoParaRetorno.setIvaNeto(new BigDecimal("26.03"));
    productoParaRetorno.setPrecioLista(new BigDecimal("150"));
    productoParaRetorno.setPorcentajeBonificacionPrecio(new BigDecimal("10"));
    productoParaRetorno.setPrecioBonificado(new BigDecimal("135"));
    productoParaRetorno.setPorcentajeBonificacionOferta(BigDecimal.ZERO);
    productoParaRetorno.setBulto(new BigDecimal("5"));
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(productoParaRetorno);
    when(productoService.getProductoNoEliminadoPorId(2L)).thenReturn(productoParaRetorno);
    assertFalse(
        facturaVentaServiceImpl
            .getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A)
            .isEmpty());
    assertFalse(
        facturaVentaServiceImpl
            .getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A)
            .isEmpty());
  }

  @Test
  void shouldTestEnviarFacturaPorEmail() {
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setIdFactura(1L);
    facturaVenta.setNumSerie(2L);
    facturaVenta.setNumFactura(1L);
    facturaVenta.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    Cliente clienteDeFactura = new Cliente();
    facturaVenta.setCliente(clienteDeFactura);
    when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaVenta));
    assertThrows(
        BusinessServiceException.class,
        () -> facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L));
    verify(messageSource).getMessage(eq("mensaje_correo_factura_sin_cae"), any(), any());
    facturaVenta.setCae(123L);
    when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaVenta));
    assertThrows(
        BusinessServiceException.class,
        () -> facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L));
    verify(messageSource).getMessage(eq("mensaje_correo_cliente_sin_email"), any(), any());
    clienteDeFactura.setEmail("correo@decliente.com");
    facturaVenta.setCliente(clienteDeFactura);
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal de test");
    facturaVenta.setSucursal(sucursal);
    ConfiguracionSucursal configuracionSucursal = new ConfiguracionSucursal();
    configuracionSucursal.setUsarFacturaVentaPreImpresa(true);
    when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaVenta));
//    when(configuracionSucursalService.getConfiguracionSucursal(sucursal))
//        .thenReturn(configuracionSucursal);
    sucursal.setConfiguracionSucursal(configuracionSucursal);
    facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L);
    Pedido pedido = new Pedido();
    pedido.setTipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL);
    pedido.setSucursal(sucursal);
    UbicacionDTO ubicacionDTO = UbicacionDTO.builder().build();
    ubicacionDTO.setCalle("Calle 123");
    pedido.setDetalleEnvio(ubicacionDTO);
    facturaVenta.setPedido(pedido);
    when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaVenta));
    facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L);
    pedido.setTipoDeEnvio(TipoDeEnvio.USAR_UBICACION_ENVIO);
    facturaVenta.setPedido(pedido);
    when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaVenta));
    facturaVentaServiceImpl.enviarFacturaVentaPorEmail(1L);
    verify(correoElectronicoService, times(3))
        .enviarEmail(
            eq(facturaVenta.getCliente().getEmail()),
            eq(""),
            eq("Su Factura de Compra"),
            any(),
            any(),
            eq("Reporte.pdf"));
  }

  @Test
  void shouldConstruirFacturaVenta() {
    Pedido pedido = new Pedido();
    pedido.setIdPedido(1L);
    Sucursal sucursal = new Sucursal();
    pedido.setSucursal(sucursal);
    List<RenglonPedido> renglones = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.TEN);
    renglones.add(renglonPedido);
    pedido.setRenglones(renglones);
    when(pedidoService.getPedidoNoEliminadoPorId(1L)).thenReturn(pedido);
    when(pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(1L)).thenReturn(renglones);
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO = new NuevaFacturaVentaDTO();
    nuevaFacturaVentaDTO.setTipoDeComprobante(TipoDeComprobante.FACTURA_A);
    nuevaFacturaVentaDTO.setIdCliente(1L); // cliente para esta factura
    nuevaFacturaVentaDTO.setIdTransportista(1L);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(new Cliente());
    assertThrows(
        BusinessServiceException.class,
        () -> facturaVentaServiceImpl.construirFacturaVenta(nuevaFacturaVentaDTO, 1L, 1L));
    verify(messageSource).getMessage(eq("mensaje_pedido_facturar_error_estado"), any(), any());
    pedido.setEstado(EstadoPedido.ABIERTO);
    assertThrows(
            BusinessServiceException.class,
            () -> facturaVentaServiceImpl.construirFacturaVenta(nuevaFacturaVentaDTO, 1L, 1L));
    verify(messageSource).getMessage(eq("mensaje_ubicacion_facturacion_vacia"), any(), any());
    Cliente cliente = new Cliente();
    cliente.setUbicacionFacturacion(new Ubicacion());
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    when(transportistaService.getTransportistaNoEliminadoPorId(1L)).thenReturn(new Transportista());
    boolean[] renglonesMarcados = new boolean[] {true, false};
    nuevaFacturaVentaDTO.setRenglonMarcado(renglonesMarcados);
    assertThrows(
        BusinessServiceException.class,
        () -> facturaVentaServiceImpl.construirFacturaVenta(nuevaFacturaVentaDTO, 1L, 1L));
    verify(messageSource)
        .getMessage(eq("mensaje_factura_renglones_marcados_incorrectos"), any(), any());
    renglonesMarcados = new boolean[] {true};
    nuevaFacturaVentaDTO.setRenglonMarcado(renglonesMarcados);
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setBulto(new BigDecimal("5"));
    Medida medidaProducto = new Medida();
    medidaProducto.setNombre("Metro");
    producto.setMedida(medidaProducto);
    producto.setIvaPorcentaje(new BigDecimal("21"));
    producto.setPrecioVentaPublico(new BigDecimal("100"));
    producto.setPrecioLista(new BigDecimal("121"));
    when(productoService.getProductoNoEliminadoPorId(1L)).thenReturn(producto);
    FacturaVenta facturaVenta =
        facturaVentaServiceImpl.construirFacturaVenta(nuevaFacturaVentaDTO, 1L, 1L);
    assertNotNull(facturaVenta);
    assertEquals(TipoDeComprobante.FACTURA_A, facturaVenta.getTipoComprobante());
    assertEquals(1L, facturaVenta.getPedido().getIdPedido());
    assertEquals(1, facturaVenta.getRenglones().size());
    assertEquals(new BigDecimal("10"), facturaVenta.getRenglones().get(0).getCantidad());
    assertEquals(new BigDecimal("100"), facturaVenta.getRenglones().get(0).getPrecioUnitario());
    assertEquals(new BigDecimal("21"), facturaVenta.getRenglones().get(0).getIvaPorcentaje());
    assertEquals(
        new BigDecimal("21.000000000000000000000000000000"),
        facturaVenta.getRenglones().get(0).getIvaNeto());
    assertEquals(new BigDecimal("1210"), facturaVenta.getRenglones().get(0).getImporteAnterior());
    assertEquals(new BigDecimal("1000"), facturaVenta.getRenglones().get(0).getImporte());
  }
}
