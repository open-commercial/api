package sic.service.impl;

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
    public void shouldcalcularGanancia_Porcentaje() {
        double precioCosto = 12.34;
        double pvp = 23.45;
        double resultadoEsperado = 90.03241491085899;
        double resultadoObtenido = productoService.calcularGanancia_Porcentaje(null ,null, pvp, null, null, precioCosto, false);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldcalcularCalcularGanancia_Neto() {
        double precioCosto = 12.34;
        double gananciaPorcentaje = 100;
        double resultadoEsperado = 12.34;
        double resultadoObtenido = productoService.calcularGanancia_Neto(precioCosto, gananciaPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldcalcularCalcularPVP() {
        double precioCosto = 12.34;
        double gananciaPorcentaje = 100;
        double resultadoEsperado = 24.68;
        double resultadoObtenido = productoService.calcularPVP(precioCosto, gananciaPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldcalcularCalcularIVA_Neto() {
        double pvp = 24.68;
        double ivaPorcentaje = 21;
        double resultadoEsperado = 5.182799999999999;
        double resultadoObtenido = productoService.calcularIVA_Neto(pvp, ivaPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldCalcularImpInterno_Neto() {
        double pvp = 24.68;
        double impuestoInternoPorcentaje = 10;
        double resultadoEsperado = 2.468;
        double resultadoObtenido = productoService.calcularImpInterno_Neto(pvp, impuestoInternoPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }
    
    @Test
    public void shouldCalcularPrecioLista() {
        double pvp = 24.68;
        double ivaPorcentaje = 21;
        double impuestoInternoPorcentaje = 10;
        double resultadoEsperado = 32.330799999999996;
        double resultadoObtenido = productoService.calcularPrecioLista(pvp, ivaPorcentaje, impuestoInternoPorcentaje);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }    
}
