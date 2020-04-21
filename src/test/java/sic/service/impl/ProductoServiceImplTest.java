package sic.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.App;
import sic.builder.ProductoBuilder;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.repository.ProductoRepository;
import sic.service.IMedidaService;
import sic.service.IProveedorService;
import sic.service.IRubroService;
import sic.service.ISucursalService;

import javax.persistence.EntityNotFoundException;

@WebMvcTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProductoServiceImpl.class})
@TestPropertySource(locations = "classpath:application.properties")
class ProductoServiceImplTest {

  @MockBean IMedidaService medidaService;
  @MockBean IRubroService rubroService;
  @MockBean IProveedorService proveedorService;
  @MockBean ISucursalService sucursalService;
  @MockBean ProductoRepository productoRepository;

  @Autowired MessageSource messageSource;
  @Autowired ProductoServiceImpl productoService;

  private Producto crearProducto() {
    Producto producto = new Producto();
    producto.setDescripcion("Disco de corte");
    Set<CantidadEnSucursal> altaCantidadesEnSucursales = new HashSet<>();
    CantidadEnSucursal cantidad = new CantidadEnSucursal();
    cantidad.setCantidad(BigDecimal.ZERO);
    cantidad.setSucursal(new Sucursal());
    altaCantidadesEnSucursales.add(cantidad);
    producto.setCantidadEnSucursales(altaCantidadesEnSucursales);
    producto.setCantidadTotalEnSucursales(BigDecimal.ZERO);
    producto.setCantMinima(BigDecimal.ONE);
    producto.setBulto(BigDecimal.ONE);
    producto.setMedida(new Medida());
    producto.setPrecioCosto(BigDecimal.ZERO);
    producto.setGananciaPorcentaje(BigDecimal.ZERO);
    producto.setGananciaNeto(BigDecimal.ZERO);
    producto.setPrecioVentaPublico(BigDecimal.ZERO);
    producto.setIvaPorcentaje(BigDecimal.ZERO);
    producto.setIvaNeto(BigDecimal.ZERO);
    producto.setPrecioLista(BigDecimal.ZERO);
    producto.setPorcentajeBonificacionOferta(BigDecimal.ZERO);
    producto.setPorcentajeBonificacionPrecio(BigDecimal.ZERO);
    producto.setPrecioBonificado(BigDecimal.ZERO);
    producto.setRubro(new Rubro());
    producto.setFechaUltimaModificacion(LocalDateTime.now());
    producto.setProveedor(new Proveedor());
    producto.setFechaAlta(LocalDateTime.now());
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
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.guardar(nuevoProductoUno, 1L, 1L, 1L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_duplicado_codigo", null, Locale.getDefault())));
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
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.guardar(nuevoProductoUno, 1L, 1L, 1L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_duplicado_descripcion", null, Locale.getDefault())));
  }

  @Test
  public void shouldThrownBusinessExceptionActualizarProductoSinImagen() {
    Producto producto = this.crearProducto();
    producto.setOferta(true);
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.actualizar(producto, producto, null));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_oferta_sin_imagen",
                    new Object[] {producto.getDescripcion()},
                    Locale.getDefault())));
  }

  @Test
  public void shouldThrownBusinessExceptionActualizacionProductoDuplicadoCodigo() {
    Producto productoParaActualizar = this.crearProducto();
    productoParaActualizar.setIdProducto(1L);
    productoParaActualizar.setCodigo("X3M.12");
    Producto productoPersistido = this.crearProducto();
    productoPersistido.setIdProducto(2L);
    productoPersistido.setCodigo("X3M.12");
    when(productoRepository.findByCodigoAndEliminado("X3M.12", false))
        .thenReturn(productoPersistido);
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.actualizar(productoParaActualizar, productoPersistido, null));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_duplicado_codigo",
                    new Object[] {productoParaActualizar.getDescripcion()},
                    Locale.getDefault())));
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
    EntityNotFoundException thrown =
        assertThrows(
            EntityNotFoundException.class, () -> productoService.getProductoNoEliminadoPorId(1L));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_no_existente", null, Locale.getDefault())));
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
  void shouldThrownBusinessServiceExceptionPorBuscarProductosSinIdSucursal() {
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
        ProductosParaVerificarStockDTO.builder().cantidad(cantidad).idProducto(idProducto).build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_consulta_stock_sin_sucursal", null, Locale.getDefault())));
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
    Producto producto = new ProductoBuilder().withId_Producto(1L).build();
    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
    productoService.actualizarMultiples(productosParaActualizarDTO);
    BusinessServiceException thrown =
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
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_modificar_producto_no_permitido", null, Locale.getDefault())));
    thrown =
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
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_error_ids_duplicados", null, Locale.getDefault())));
  }
}
