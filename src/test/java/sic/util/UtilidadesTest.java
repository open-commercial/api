package sic.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilidadesTest {
    
    private static final int CANTIDAD_DECIMALES_TRUNCAMIENTO = 2;

    @Test
    public void shouldTruncarDecimal() {
        double resultadoEsperado = 22.22;
        double resultadoObtenido = Utilidades.truncarDecimal(22.22959446846487, CANTIDAD_DECIMALES_TRUNCAMIENTO);
        assertEquals(resultadoEsperado, resultadoObtenido, 0);
    }

}
