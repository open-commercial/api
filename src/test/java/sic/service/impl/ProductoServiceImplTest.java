package sic.service.impl;

import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ProductoServiceImplTest {
    
    @InjectMocks
    private ProductoServiceImpl productoService;
    
    @Test
    public void shouldCalcularGanancia_Porcentaje() {
        double precioCosto = 12.34;
        double pvp = 23.45;
        double resultadoEsperado = 90.03241491085899;
        double resultadoObtenido = productoService.calcularGanancia_Porcentaje(null ,null, new BigDecimal(pvp), null, null, new BigDecimal(precioCosto), false).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldCalcularGanancia_Neto() {
        double precioCosto = 12.34;
        double gananciaPorcentaje = 100;
        double resultadoEsperado = 12.34;
        double resultadoObtenido = productoService.calcularGanancia_Neto(new BigDecimal(precioCosto), new BigDecimal(gananciaPorcentaje)).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldcalcularCalcularPVP() {
        double precioCosto = 12.34;
        double gananciaPorcentaje = 100;
        double resultadoEsperado = 24.68;
        double resultadoObtenido = productoService.calcularPVP(new BigDecimal(precioCosto), new BigDecimal(gananciaPorcentaje)).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldCalcularIVA_Neto() {
        double pvp = 24.68;
        double ivaPorcentaje = 21;
        double resultadoEsperado = 5.1828;
        double resultadoObtenido = productoService.calcularIVA_Neto(new BigDecimal(pvp), new BigDecimal(ivaPorcentaje)).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldCalcularImpInterno_Neto() {
        double pvp = 24.68;
        double impuestoInternoPorcentaje = 10;
        double resultadoEsperado = 2.468;
        double resultadoObtenido = productoService.calcularImpInterno_Neto(new BigDecimal(pvp), new BigDecimal(impuestoInternoPorcentaje)).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldCalcularPrecioLista() {
        double pvp = 24.68;
        double ivaPorcentaje = 21;
        double impuestoInternoPorcentaje = 10;
        double resultadoEsperado = 32.330799999999996;
        double resultadoObtenido = productoService.calcularPrecioLista(new BigDecimal(pvp), new BigDecimal(ivaPorcentaje), new BigDecimal(impuestoInternoPorcentaje)).doubleValue();
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }    
}
