package sic.service.impl;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ProductoServiceImplTest {

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
