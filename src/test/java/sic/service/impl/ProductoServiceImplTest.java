package sic.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.repository.ProductoRepository;
import sic.util.CustomValidator;

import javax.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {ProductoServiceImpl.class, CustomValidator.class, MessageSource.class})
class ProductoServiceImplTest {

  @MockBean MedidaServiceImpl medidaService;
  @MockBean RubroServiceImpl rubroService;
  @MockBean ProveedorServiceImpl proveedorService;
  @MockBean SucursalServiceImpl sucursalService;
  @MockBean TraspasoServiceImpl traspasoService;
  @MockBean PedidoServiceImpl pedidoService;
  @MockBean ProductoRepository productoRepository;
  @MockBean MessageSource messageSource;

  @Autowired ProductoServiceImpl productoService;

  private Producto construirProducto() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCodigo("1");
    producto.setDescripcion("Cinta adhesiva doble faz 3M");
    producto.setMedida(new Medida());
    producto.setPrecioCosto(new BigDecimal("89.35"));
    producto.setGananciaPorcentaje(new BigDecimal("38.74"));
    producto.setGananciaNeto(new BigDecimal("34.614"));
    producto.setPrecioVentaPublico(new BigDecimal("123.964"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    producto.setIvaNeto(new BigDecimal("26.032"));
    producto.setPrecioLista(new BigDecimal("150"));
    producto.setPorcentajeBonificacionPrecio(new BigDecimal("10"));
    producto.setPrecioBonificado(new BigDecimal("135"));
    producto.setPorcentajeBonificacionOferta(BigDecimal.ZERO);
    producto.setBulto(new BigDecimal("5"));
    producto.setFechaAlta(LocalDateTime.now());
    producto.setFechaUltimaModificacion(LocalDateTime.now());
    Sucursal sucursal = new Sucursal();
    Rubro rubro = new Rubro();
    Proveedor proveedor = new Proveedor();
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setCantidad(BigDecimal.TEN);
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursales.add(cantidadEnSucursal);
    cantidadEnSucursal.setSucursal(sucursal);
    producto.setCantidadEnSucursales(cantidadEnSucursales);
    producto.setCantidadTotalEnSucursales(BigDecimal.TEN);
    producto.setRubro(rubro);
    producto.setProveedor(proveedor);
    return producto;
  }

  @Test
  void shouldCalcularGananciaPorcentajeDescendente() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal pvp = new BigDecimal("23.45");
    BigDecimal resultadoEsperado = new BigDecimal("90.032414910859000");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaPorcentaje(null, null, pvp, null, null, precioCosto, false);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularGananciaPorcentajeAscendente() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal pvp = new BigDecimal("23.45");
    BigDecimal resultadoEsperado = new BigDecimal("95.223662884927066");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaPorcentaje(
            new BigDecimal("30"),
            new BigDecimal("25"),
            pvp,
            new BigDecimal("21"),
            BigDecimal.ZERO,
            precioCosto,
            true);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularGananciaNeto() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal gananciaPorcentaje = new BigDecimal("100");
    BigDecimal resultadoEsperado = new BigDecimal("12.340000000000000");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaNeto(precioCosto, gananciaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldcalcularCalcularPVP() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal gananciaPorcentaje = new BigDecimal("100");
    BigDecimal resultadoEsperado = new BigDecimal("24.68000000000000000");
    BigDecimal resultadoObtenido = productoService.calcularPVP(precioCosto, gananciaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularIVANeto() {
    BigDecimal pvp = new BigDecimal("24.68");
    BigDecimal ivaPorcentaje = new BigDecimal("21");
    BigDecimal resultadoEsperado = new BigDecimal("5.182800000000000");
    BigDecimal resultadoObtenido = productoService.calcularIVANeto(pvp, ivaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldCalcularPrecioLista() {
    BigDecimal pvp = new BigDecimal("24.68");
    BigDecimal ivaPorcentaje = new BigDecimal("21");
    BigDecimal resultadoEsperado = new BigDecimal("29.86280000000000000");
    BigDecimal resultadoObtenido = productoService.calcularPrecioLista(pvp, ivaPorcentaje);
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldThrownBusinessExceptionAltaProductoCodigoDuplicado() {
    NuevoProductoDTO nuevoProductoUno =
        NuevoProductoDTO.builder()
            .descripcion("Ventilador de pie")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, BigDecimal.TEN);
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(new BigDecimal("100"))
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .porcentajeBonificacionOferta(new BigDecimal("-1"))
            .oferta(true)
            .imagen((new String("imagen")).getBytes())
            .codigo("12345")
            .build();
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(new Proveedor());
    when(rubroService.getRubroNoEliminadoPorId(1L)).thenReturn(new Rubro());
    when(medidaService.getMedidaNoEliminadaPorId(1L)).thenReturn(new Medida());
    Sucursal[] sucursales = new Sucursal[] {new Sucursal()};
    when(sucursalService.getSucusales(false)).thenReturn(Arrays.asList(sucursales));
    when(productoRepository.findByDescripcionAndEliminado("Ventilador de pie", false))
        .thenReturn(null);
    Producto productoDuplicado = new Producto();
    productoDuplicado.setDescripcion("12345");
    when(productoRepository.findByCodigoAndEliminado("12345", false)).thenReturn(productoDuplicado);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.guardar(nuevoProductoUno, 1L, 1L, 1L));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_codigo"), any(), any());
  }

  @Test
  void shouldThrownBusinessExceptionAltaProductoDescripcionDuplicado() {
    NuevoProductoDTO nuevoProductoUno =
        NuevoProductoDTO.builder()
            .descripcion("Ventilador de pie")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, BigDecimal.TEN);
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(new BigDecimal("100"))
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .porcentajeBonificacionOferta(new BigDecimal("-1"))
            .oferta(true)
            .imagen((new String("imagen")).getBytes())
            .codigo("12345")
            .build();
    when(proveedorService.getProveedorNoEliminadoPorId(1L)).thenReturn(new Proveedor());
    when(rubroService.getRubroNoEliminadoPorId(1L)).thenReturn(new Rubro());
    when(medidaService.getMedidaNoEliminadaPorId(1L)).thenReturn(new Medida());
    Sucursal[] sucursales = new Sucursal[] {new Sucursal()};
    when(sucursalService.getSucusales(false)).thenReturn(Arrays.asList(sucursales));
    Producto productoDuplicado = new Producto();
    productoDuplicado.setDescripcion("Ventilador de pie");
    when(productoRepository.findByDescripcionAndEliminado("Ventilador de pie", false))
        .thenReturn(productoDuplicado);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.guardar(nuevoProductoUno, 1L, 1L, 1L));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_descripcion"), any(), any());
  }

  @Test
  void shouldThrownBusinessExceptionActualizarProductoSinImagen() {
    Producto producto = this.construirProducto();
    producto.setOferta(true);
    assertThrows(
        BusinessServiceException.class, () -> productoService.actualizar(producto, producto, null));
    verify(messageSource).getMessage(eq("mensaje_producto_oferta_sin_imagen"), any(), any());
  }

  @Test
  void shouldThrownBusinessExceptionActualizacionProductoDuplicadoCodigo() {
    Producto productoParaActualizar = this.construirProducto();
    productoParaActualizar.setIdProducto(1L);
    Producto productoPersistido = this.construirProducto();
    productoPersistido.setIdProducto(2L);
    when(productoRepository.findByDescripcionAndEliminado("Cinta adhesiva doble faz 3M", false))
        .thenReturn(productoPersistido);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.actualizar(productoParaActualizar, productoPersistido, null));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_descripcion"), any(), any());
  }

  @Test
  void shouldTestBusquedaCriteria() {
    BusquedaProductoCriteria criteria =
        BusquedaProductoCriteria.builder()
            .codigo("213")
            .descripcion("testDescripcion")
            .idRubro(1L)
            .idProveedor(2L)
            .listarSoloEnStock(true)
            .listarSoloEnStock(true)
            .publico(true)
            .oferta(true)
            .build();
    String stringBuilder =
        "producto.eliminado = false && (containsIc(producto.codigo,213) || containsIc(producto.descripcion,testDescripcion)) "
            + "&& producto.rubro.idRubro = 1 && producto.proveedor.idProveedor = 2 "
            + "&& any(producto.cantidadEnSucursales).cantidad > 0 && producto.ilimitado = false && producto.publico = true "
            + "&& producto.oferta = true";
    assertEquals(stringBuilder, productoService.getBuilder(criteria).toString());
  }

  @Test
  void shouldGetProductoConPrecioBonificadoPorOferta() {
    Producto producto = new Producto();
    producto.setPrecioLista(new BigDecimal("100"));
    producto.setPorcentajeBonificacionOferta(new BigDecimal("10"));
    producto.setOferta(true);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    Producto productoRecuperado = productoService.getProductoNoEliminadoPorId(1L);
    assertEquals(new BigDecimal("100"), productoRecuperado.getPrecioLista());
    assertEquals(new BigDecimal("90.00"), productoRecuperado.getPrecioBonificado());
  }

  @Test
  void shouldThrownEntityNotFoundException() {
    when(productoRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class, () -> productoService.getProductoNoEliminadoPorId(1L));
    verify(messageSource).getMessage(eq("mensaje_producto_no_existente"), any(), any());
  }

  @Test
  void shouldDevolverNullSiCodigoVacio() {
    assertNull(productoService.getProductoPorCodigo("123"));
  }

  @Test
  void shouldGetProductosSinStockDisponible() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCantidadTotalEnSucursales(new BigDecimal("9"));
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursal.setCantidad(new BigDecimal("9"));
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    cantidadEnSucursales.add(cantidadEnSucursal);
    producto.setCantidadEnSucursales(cantidadEnSucursales);
    producto.setIlimitado(false);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    long[] idProducto = {1};
    BigDecimal[] cantidad = {BigDecimal.TEN.add(BigDecimal.ONE)};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder().build();
    productosParaVerificarStockDTO.setIdSucursal(1L);
    productosParaVerificarStockDTO.setCantidad(cantidad);
    productosParaVerificarStockDTO.setIdProducto(idProducto);
    List<ProductoFaltanteDTO> resultadoObtenido =
        productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
    Assertions.assertFalse(resultadoObtenido.isEmpty());
    assertEquals(new BigDecimal("11"), resultadoObtenido.get(0).getCantidadSolicitada());
    assertEquals(new BigDecimal("9"), resultadoObtenido.get(0).getCantidadDisponible());
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglonPedido.setCantidad(BigDecimal.ONE);
    renglonesPedido.add(renglonPedido);
    when(pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(1L)).thenReturn(renglonesPedido);
    productosParaVerificarStockDTO =
            ProductosParaVerificarStockDTO.builder().build();
    productosParaVerificarStockDTO.setIdSucursal(1L);
    productosParaVerificarStockDTO.setCantidad(cantidad);
    productosParaVerificarStockDTO.setIdProducto(idProducto);
    productosParaVerificarStockDTO.setIdPedido(1L);
    resultadoObtenido =
            productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
    Assertions.assertFalse(resultadoObtenido.isEmpty());
    assertEquals(new BigDecimal("10"), resultadoObtenido.get(0).getCantidadSolicitada());
    assertEquals(new BigDecimal("9"), resultadoObtenido.get(0).getCantidadDisponible());
  }

  @Test
  void shouldTestActualizarMultiplesProductos() {
    ProductosParaActualizarDTO productosParaActualizarDTO =
        ProductosParaActualizarDTO.builder()
            .idProducto(new long[] {1L})
            .cantidadVentaMinima(BigDecimal.TEN)
            .idMedida(1L)
            .idRubro(1L)
            .idProveedor(2L)
            .gananciaPorcentaje(BigDecimal.TEN)
            .ivaPorcentaje(new BigDecimal("21"))
            .precioCosto(BigDecimal.TEN)
            .porcentajeBonificacionPrecio(BigDecimal.TEN)
            .publico(true)
            .build();
    Producto producto = this.construirProducto();
    producto.setIdProducto(1L);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    productoService.actualizarMultiples(productosParaActualizarDTO);
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarMultiples(
                ProductosParaActualizarDTO.builder()
                    .idProducto(new long[] {1L})
                    .descuentoRecargoPorcentaje(BigDecimal.TEN)
                    .cantidadVentaMinima(BigDecimal.TEN)
                    .idMedida(1L)
                    .idRubro(1L)
                    .idProveedor(2L)
                    .gananciaPorcentaje(BigDecimal.TEN)
                    .ivaPorcentaje(new BigDecimal("21"))
                    .precioCosto(BigDecimal.TEN)
                    .porcentajeBonificacionPrecio(BigDecimal.TEN)
                    .publico(true)
                    .build()));
    verify(messageSource).getMessage(eq("mensaje_modificar_producto_no_permitido"), any(), any());
    assertThrows(
        BusinessServiceException.class,
        () ->
            productoService.actualizarMultiples(
                ProductosParaActualizarDTO.builder()
                    .idProducto(new long[] {1L, 1L})
                    .cantidadVentaMinima(BigDecimal.TEN)
                    .idMedida(1L)
                    .idRubro(1L)
                    .idProveedor(2L)
                    .gananciaPorcentaje(BigDecimal.TEN)
                    .ivaPorcentaje(new BigDecimal("21"))
                    .precioCosto(BigDecimal.TEN)
                    .porcentajeBonificacionPrecio(BigDecimal.TEN)
                    .publico(true)
                    .build()));
    verify(messageSource).getMessage(eq("mensaje_error_ids_duplicados"), any(), any());
  }

  @Test
  void shouldDevolverStockPedido() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    List<RenglonPedido> renglones = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglones.add(renglonPedido);
    pedido.setRenglones(renglones);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    pedido.setSucursal(sucursal);
    List<RenglonPedido> renglonesAnteriores = new ArrayList<>();
    RenglonPedido renglonPedidoAnterior = new RenglonPedido();
    renglonPedidoAnterior.setIdProductoItem(1L);
    renglonesAnteriores.add(renglonPedido);
    productoService.devolverStockPedido(pedido,TipoDeOperacion.ACTUALIZACION,  renglonesAnteriores, 1L);
    verify(messageSource).getMessage(eq("mensaje_error_actualizar_stock_producto_eliminado"), any(), any());
    Producto producto = this.construirProducto();
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    when(productoRepository.save(producto)).thenReturn(producto);
    productoService.devolverStockPedido(pedido, TipoDeOperacion.ACTUALIZACION, renglonesAnteriores, 1L);
    verify(messageSource).getMessage(eq("mensaje_producto_agrega_stock"), any(), any());
    verify(productoRepository).save(producto);
  }

  @Test
  void shouldActualizarStockPedido() {
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.ABIERTO);
    List<RenglonPedido> renglones = new ArrayList<>();
    RenglonPedido renglonPedido = new RenglonPedido();
    renglonPedido.setIdProductoItem(1L);
    renglones.add(renglonPedido);
    pedido.setRenglones(renglones);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    pedido.setSucursal(sucursal);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    verify(messageSource).getMessage(eq("mensaje_error_actualizar_stock_producto_eliminado"), any(), any());
    Producto producto = this.construirProducto();
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    when(productoRepository.save(producto)).thenReturn(producto);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    verify(messageSource, times(2)).getMessage(eq("mensaje_producto_quita_stock"), any(), any());
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ELIMINACION);
    pedido.setEstado(EstadoPedido.CANCELADO);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    verify(messageSource, times(2)).getMessage(eq("mensaje_producto_agrega_stock"), any(), any());
    verify(traspasoService, times(3)).guardarTraspasosPorPedido(pedido);
    verify(traspasoService, times(3)).eliminarTraspasoDePedido(pedido);
  }

  @Test
  void shouldValidarReglasDeNegocio() {
    Producto producto = this.construirProducto();
    producto.setIdProducto(1L);
    producto.setOferta(true);
    producto.setPorcentajeBonificacionOferta(new BigDecimal("-1"));
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.validarReglasDeNegocio(TipoDeOperacion.ALTA, producto));
    verify(messageSource).getMessage(eq("mensaje_producto_oferta_inferior_0"), any(), any());
    producto.setPorcentajeBonificacionOferta(new BigDecimal("10"));
    Producto productoDuplicado = this.construirProducto();
    productoDuplicado.setIdProducto(2L);
    when(productoRepository.findByCodigoAndEliminado(producto.getCodigo(), false))
        .thenReturn(productoDuplicado);
    assertThrows(
        BusinessServiceException.class,
        () -> productoService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, producto));
    verify(messageSource).getMessage(eq("mensaje_producto_duplicado_codigo"), any(), any());
  }

  @Test
  void shouldValidarCalculos() {
    Producto producto = this.construirProducto();
    producto.setIvaPorcentaje(BigDecimal.TEN);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_error_iva_no_valido"), any(), any());
    producto.setIvaPorcentaje(new BigDecimal("21.0"));
    BigDecimal valorAuxiliar = producto.getGananciaNeto();
    producto.setGananciaNeto(BigDecimal.ONE);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_producto_ganancia_neta_incorrecta"), any(), any());
    producto.setGananciaNeto(valorAuxiliar);
    valorAuxiliar = producto.getIvaNeto();
    producto.setIvaNeto(BigDecimal.ONE);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_producto_iva_neto_incorrecto"), any(), any());
    producto.setIvaNeto(valorAuxiliar);
    producto.setPrecioLista(BigDecimal.ONE);
    assertThrows(BusinessServiceException.class, () -> productoService.validarCalculos(producto));
    verify(messageSource).getMessage(eq("mensaje_producto_precio_lista_incorrecto"), any(), any());
  }

  @Test
  void shouldActualizarStockTraspaso() {
    Producto producto1 = new Producto();
    producto1.setIdProducto(1L);
    producto1.setCantidadTotalEnSucursales(new BigDecimal("1"));
    producto1.setDescripcion("Ventilador de pie");
    Sucursal sucursalOrigen = new Sucursal();
    sucursalOrigen.setIdSucursal(1L);
    CantidadEnSucursal cantidadEnSucursalProducto1 = new CantidadEnSucursal();
    cantidadEnSucursalProducto1.setSucursal(sucursalOrigen);
    cantidadEnSucursalProducto1.setCantidad(new BigDecimal("1"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto1 = new HashSet<>();
    cantidadEnSucursalesProducto1.add(cantidadEnSucursalProducto1);
    producto1.setCantidadEnSucursales(cantidadEnSucursalesProducto1);
    producto1.setIlimitado(false);
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
    when(productoRepository.save(producto1)).thenReturn(producto1);
    Producto producto2 = new Producto();
    producto2.setIdProducto(2L);
    producto2.setCantidadTotalEnSucursales(new BigDecimal("2"));
    producto2.setDescripcion("Duchas");
    CantidadEnSucursal cantidadEnSucursalProducto2 = new CantidadEnSucursal();
    cantidadEnSucursalProducto2.setSucursal(sucursalOrigen);
    cantidadEnSucursalProducto2.setCantidad(new BigDecimal("2"));
    Set<CantidadEnSucursal> cantidadEnSucursalesProducto2 = new HashSet<>();
    cantidadEnSucursalesProducto2.add(cantidadEnSucursalProducto2);
    producto2.setCantidadEnSucursales(cantidadEnSucursalesProducto2);
    producto2.setIlimitado(false);
    when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
    when(productoRepository.save(producto2)).thenReturn(producto2);
    Traspaso traspaso = new Traspaso();
    List<RenglonTraspaso> renglones = new ArrayList<>();
    RenglonTraspaso renglonTraspaso1 = new RenglonTraspaso();
    renglonTraspaso1.setIdProducto(1L);
    renglonTraspaso1.setCantidadProducto(BigDecimal.TEN);
    RenglonTraspaso renglonTraspaso2 = new RenglonTraspaso();
    renglonTraspaso2.setIdProducto(2L);
    renglonTraspaso2.setCantidadProducto(BigDecimal.ONE);
    renglones.add(renglonTraspaso1);
    renglones.add(renglonTraspaso2);
    traspaso.setRenglones(renglones);
    traspaso.setSucursalOrigen(sucursalOrigen);
    Sucursal sucursalDestino = new Sucursal();
    sucursalDestino.setIdSucursal(2L);
    traspaso.setSucursalDestino(sucursalDestino);
    assertThrows(
            BusinessServiceException.class, () -> productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA));
    verify(messageSource).getMessage(eq("mensaje_traspaso_sin_stock"), any(), any());

    producto1.setCantidadTotalEnSucursales(BigDecimal.TEN);
    producto1
        .getCantidadEnSucursales()
        .forEach(cantidadEnSucursal -> cantidadEnSucursal.setCantidad(BigDecimal.TEN));
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA);
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ELIMINACION);
    verify(productoRepository, times(8)).save(any());
    assertThrows(
            BusinessServiceException.class, () -> productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ACTUALIZACION));
    verify(messageSource).getMessage(eq("mensaje_traspaso_operacion_no_soportada"), any(), any());
  }
}
