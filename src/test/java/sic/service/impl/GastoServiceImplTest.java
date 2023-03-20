package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.entity.criteria.BusquedaGastoCriteria;
import sic.repository.GastoRepository;
import sic.service.ICajaService;
import sic.service.ISucursalService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GastoServiceImpl.class, MessageSource.class})
class GastoServiceImplTest {

    @MockBean
    GastoRepository gastoRepository;
    @MockBean
    ISucursalService sucursalService;
    @MockBean
    ICajaService cajaService;
    @MockBean
    MessageSource messageSource;
    @Autowired
    GastoServiceImpl gastoService;

    @Test
    void shouldTestGastosSearchWithOnlyFechaDesde() {
        BusquedaGastoCriteria busquedaGastoCriteria = BusquedaGastoCriteria.builder()
                .fechaDesde(LocalDateTime.MIN)
                .build();
        String builder =
                "gasto.fecha > -999999999-01-01T00:00";
        assertEquals(builder, gastoService.getBuilder(busquedaGastoCriteria).toString());
    }

    @Test
    void shouldTestGastosSearchWithOnlyFechaHasta() {
        BusquedaGastoCriteria busquedaGastoCriteria = BusquedaGastoCriteria.builder()
                .fechaHasta(LocalDateTime.MAX)
                .build();
        String builder =
                "gasto.fecha < +999999999-12-31T23:59:59.999999999";
        assertEquals(builder, gastoService.getBuilder(busquedaGastoCriteria).toString());
    }

    @Test
    void shouldTestGastosSearchWithOnlyBothDates() {
        BusquedaGastoCriteria busquedaGastoCriteria = BusquedaGastoCriteria.builder()
                .fechaDesde(LocalDateTime.MIN)
                .fechaHasta(LocalDateTime.MAX)
                .build();
        String builder =
                "gasto.fecha between -999999999-01-01T00:00 and +999999999-12-31T23:59:59.999999999";
        assertEquals(builder, gastoService.getBuilder(busquedaGastoCriteria).toString());
    }
}
