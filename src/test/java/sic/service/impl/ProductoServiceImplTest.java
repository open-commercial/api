package sic.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.NuevoProductoDTO;
import sic.repository.ProductoRepository;
import sic.service.IMedidaService;
import sic.service.IProveedorService;
import sic.service.IRubroService;
import sic.service.ISucursalService;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "messages.properties")
class ProductoServiceImplTest {

  @Autowired
  private MessageSource messageSource;

  @Mock private IMedidaService medidaService;
  @Mock private IRubroService rubroService;
  @Mock private IProveedorService proveedorService;
  @Mock private ISucursalService sucursalService;
  @Mock private ProductoRepository productoRepository;
  @Mock private MessageSource messageSourceMock;

  @InjectMocks private ProductoServiceImpl productoService;

  @Test
  void shouldCalcularGananciaPorcentaje() {
    BigDecimal precioCosto = new BigDecimal("12.34");
    BigDecimal pvp = new BigDecimal("23.45");
    BigDecimal resultadoEsperado = new BigDecimal("90.032414910859000");
    BigDecimal resultadoObtenido =
        productoService.calcularGananciaPorcentaje(null, null, pvp, null, null, precioCosto, false);
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
  public void shouldThrownProductoDuplicadoCodigoBusinessException() {
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
    when(messageSourceMock.getMessage(
            "mensaje_producto_duplicado_codigo", null, Locale.getDefault()))
        .thenReturn(
            messageSource.getMessage(
                "mensaje_producto_duplicado_codigo", null, Locale.getDefault()));
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
  public void shouldThrownProductoDescripcionBusinessException() {
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
    when(messageSourceMock.getMessage(
            "mensaje_producto_duplicado_descripcion", null, Locale.getDefault()))
            .thenReturn(
                    messageSource.getMessage(
                            "mensaje_producto_duplicado_descripcion", null, Locale.getDefault()));
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

//  @Test
//  void shouldGetProductosSinStockDisponible() {
//    Producto producto = new Producto();
//    producto.setIdProducto(1L);
//    producto.setCantidadTotalEnSucursales(BigDecimal.TEN);
//    //producto.setCantidadEnSucursales();
//    producto.setIlimitado(false);
//    when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
//    long[] idProducto = {1};
//    BigDecimal[] cantidad = {BigDecimal.TEN.add(BigDecimal.ONE)};
//    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
//        ProductosParaVerificarStockDTO.builder().cantidad(cantidad).idProducto(idProducto).build();
//    Map<Long, BigDecimal> resultadoObtenido =
//        productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
//    Assertions.assertFalse(resultadoObtenido.isEmpty());
//  }
}
