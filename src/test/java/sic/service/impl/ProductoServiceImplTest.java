package sic.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    producto.setGananciaNeto(new BigDecimal("34.62"));
    producto.setPrecioVentaPublico(new BigDecimal("123.97"));
    producto.setIvaPorcentaje(new BigDecimal("21"));
    producto.setIvaNeto(new BigDecimal("26.03"));
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
  public void shouldThrownBusinessExceptionAltaProductoCodigoDuplicado() {
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
  public void shouldThrownBusinessExceptionAltaProductoDescripcionDuplicado() {
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
  public void shouldThrownBusinessExceptionActualizarProductoSinImagen() {
    Producto producto = this.construirProducto();
    producto.setOferta(true);
    assertThrows(
        BusinessServiceException.class, () -> productoService.actualizar(producto, producto, null));
    verify(messageSource).getMessage(eq("mensaje_producto_oferta_sin_imagen"), any(), any());
  }

  @Test
  public void shouldThrownBusinessExceptionActualizacionProductoDuplicadoCodigo() {
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
  public void shouldTestBusquedaCriteria() {
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
  public void shouldGetProductoConPrecioBonificadoPorOferta() {
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
  public void shouldThrownEntityNotFoundException() {
    when(productoRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class, () -> productoService.getProductoNoEliminadoPorId(1L));
    verify(messageSource).getMessage(eq("mensaje_producto_no_existente"), any(), any());
  }

  @Test
  public void shouldDevolverNullSiCodigoVacio() {
    assertNull(productoService.getProductoPorCodigo("123"));
  }

  @Test
  void shouldGetProductosSinStockDisponible() {
    Producto producto = new Producto();
    producto.setIdProducto(1L);
    producto.setCantidadTotalEnSucursales(BigDecimal.TEN);
    Sucursal sucursal = new Sucursal();
    sucursal.setIdSucursal(1L);
    CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
    cantidadEnSucursal.setSucursal(sucursal);
    cantidadEnSucursal.setCantidad(BigDecimal.TEN);
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
    productoService.devolverStockPedido(pedido,TipoDeOperacion.ACTUALIZACION,  renglonesAnteriores);
    verify(messageSource).getMessage(eq("mensaje_error_actualizar_stock_producto_eliminado"), any(), any());
    Producto producto = this.construirProducto();
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    when(productoRepository.save(producto)).thenReturn(producto);
    productoService.devolverStockPedido(pedido,TipoDeOperacion.ACTUALIZACION,  renglonesAnteriores);
    verify(messageSource).getMessage(eq("mensaje_producto_agrega_stock"), any(), any());
    verify(productoRepository).save(producto);
  }

  @Test
  void shouldActualizarStockPedido(){
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
  }
}
