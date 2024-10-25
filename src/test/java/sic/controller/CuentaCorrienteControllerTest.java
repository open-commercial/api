package sic.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.service.AuthService;
import sic.service.ClienteService;
import sic.service.CuentaCorrienteService;
import sic.service.ProveedorService;
import sic.util.FormatoReporte;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CuentaCorrienteController.class})
class CuentaCorrienteControllerTest {

    @MockBean CuentaCorrienteService cuentaCorrienteService;
    @MockBean ProveedorService proveedorService;
    @MockBean ClienteService clienteService;
    @MockBean AuthService authService;
    @MockBean MessageSource messageSource;

    @Autowired CuentaCorrienteController cuentaCorrienteController;

    @Test
    void shouldTestReporteListaDeCuentasCorrienteClientePorCriteria() {
        BusquedaCuentaCorrienteClienteCriteria criteria = BusquedaCuentaCorrienteClienteCriteria.builder().build();
        var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        when(cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                criteria, 1L, FormatoReporte.XLSX)).thenReturn("reporte".getBytes());
        assertNotNull(cuentaCorrienteController.getReporteListaDeCuentasCorrienteClientePorCriteria(
                criteria, "xlsx", "headers"));
        when(cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(
                criteria, 1L, FormatoReporte.PDF)).thenReturn("reporte".getBytes());
        assertNotNull(cuentaCorrienteController.getReporteListaDeCuentasCorrienteClientePorCriteria(
                criteria, "pdf", "headers"));
    }
}
