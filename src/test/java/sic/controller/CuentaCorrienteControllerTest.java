package sic.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.domain.Rol;
import sic.entity.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.service.IAuthService;
import sic.service.IClienteService;
import sic.service.ICuentaCorrienteService;
import sic.service.IProveedorService;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CuentaCorrienteController.class})
class CuentaCorrienteControllerTest {

    @MockBean
    ICuentaCorrienteService cuentaCorrienteService;
    @MockBean
    IProveedorService proveedorService;
    @MockBean
    IClienteService clienteService;
    @MockBean
    IAuthService authService;
    @MockBean
    MessageSource messageSource;

    @Autowired
    CuentaCorrienteController cuentaCorrienteController;

    @Test
    void shouldTestReporteListaDeCuentasCorrienteClientePorCriteria() {
        BusquedaCuentaCorrienteClienteCriteria criteria = BusquedaCuentaCorrienteClienteCriteria.builder().build();
        LocalDateTime today = LocalDateTime.now();
        ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
        ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
        List<Rol> roles = Collections.singletonList(Rol.ADMINISTRADOR);
        SecretKey secretKey = MacProvider.generateKey();
        String token =
                Jwts.builder()
                        .setIssuedAt(Date.from(zdtNow.toInstant()))
                        .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                        .signWith(SignatureAlgorithm.HS512, secretKey)
                        .claim("idUsuario", 1L)
                        .claim("roles", roles)
                        .compact();
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        when(cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(criteria, 1L, "xlsx")).thenReturn("reporte".getBytes());
        assertNotNull(cuentaCorrienteController.getReporteListaDeCuentasCorrienteClientePorCriteria(criteria, "xlsx", "headers"));
        when(cuentaCorrienteService.getReporteListaDeCuentasCorrienteClientePorCriteria(criteria, 1L, "pdf")).thenReturn("reporte".getBytes());
        assertNotNull(cuentaCorrienteController.getReporteListaDeCuentasCorrienteClientePorCriteria(criteria, "pdf", "headers"));
    }
}
