package sic.service.impl;

import java.math.BigDecimal;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import sic.modelo.Producto;
import sic.repository.ProductoRepository;

@RunWith(SpringRunner.class)
public class ProductoServiceImplTest {
    
    @InjectMocks
    private ProductoServiceImpl productoService;

    @Mock
    private ProductoRepository productoRepository;

    @Test
    public void shouldCalcularGanancia_Porcentaje() {
        BigDecimal precioCosto = new BigDecimal("12.34");
        BigDecimal pvp = new BigDecimal("23.45");
        BigDecimal resultadoEsperado = new BigDecimal("90.032414910859000");
        BigDecimal resultadoObtenido = productoService.calcularGananciaPorcentaje(null,
                null, pvp, null, null, precioCosto, false);
        assertEquals(resultadoEsperado, resultadoObtenido);
    }
    
    @Test
    public void shouldCalcularGanancia_Neto() {
        BigDecimal precioCosto = new BigDecimal("12.34");
        BigDecimal gananciaPorcentaje = new BigDecimal("100");
        BigDecimal resultadoEsperado = new BigDecimal("12.340000000000000");
        BigDecimal resultadoObtenido = productoService.calcularGananciaNeto(precioCosto, gananciaPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido);
    }
    
    @Test
    public void shouldcalcularCalcularPVP() {
        BigDecimal precioCosto = new BigDecimal("12.34");
        BigDecimal gananciaPorcentaje = new BigDecimal("100");
        BigDecimal resultadoEsperado = new BigDecimal("24.68000000000000000");
        BigDecimal resultadoObtenido = productoService.calcularPVP(precioCosto, gananciaPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido);
    }
    
    @Test
    public void shouldCalcularIVA_Neto() {
        BigDecimal pvp = new BigDecimal("24.68");
        BigDecimal ivaPorcentaje = new BigDecimal("21");
        BigDecimal resultadoEsperado = new BigDecimal("5.182800000000000");
        BigDecimal resultadoObtenido = productoService.calcularIVANeto(pvp, ivaPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido);
    }
    
    @Test
    public void shouldCalcularImpInterno_Neto() {
        BigDecimal pvp = new BigDecimal("24.68");
        BigDecimal impuestoInternoPorcentaje = new BigDecimal("10");
        BigDecimal resultadoEsperado = new BigDecimal("2.468000000000000");
        BigDecimal resultadoObtenido = productoService.calcularImpInternoNeto(pvp, impuestoInternoPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido);
    }
    
    @Test
    public void shouldCalcularPrecioLista() {
        BigDecimal pvp = new BigDecimal("24.68");
        BigDecimal ivaPorcentaje = new BigDecimal("21");
        BigDecimal impuestoInternoPorcentaje = new BigDecimal("10");
        BigDecimal resultadoEsperado = new BigDecimal("32.33080000000000000");
        BigDecimal resultadoObtenido = productoService.calcularPrecioLista(pvp, ivaPorcentaje, impuestoInternoPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido);
    }

    @Test
    public void shouldGetProductosSinStockDisponible() {
        Producto producto = new Producto();
        producto.setId_Producto(1);
        producto.setCantidad(BigDecimal.TEN);
        producto.setIlimitado(false);
        when(productoRepository.findOne(1L)).thenReturn(producto);
        when(productoService.getProductoPorId(1)).thenReturn(producto);
        long[] idProducto = {1};
        BigDecimal[] cantidad = {BigDecimal.TEN.add(BigDecimal.ONE)};
        Map<Long, BigDecimal> resultadoObtenido = productoService.getProductosSinStockDisponible(idProducto, cantidad);
        assertFalse("El producto no debería poseer stock disponible.", resultadoObtenido.isEmpty());
    }

}
