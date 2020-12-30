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
import sic.modelo.Rol;
import sic.modelo.Sucursal;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.service.impl.AuthServiceImpl;
import sic.service.impl.CarritoCompraServiceImpl;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CarritoCompraController.class, MessageSource.class})
class CarritoCompraControllerTest {

  @MockBean CarritoCompraServiceImpl carritoCompraService;
  @MockBean AuthServiceImpl authService;
  @MockBean MessageSource messageSource;

  @Autowired CarritoCompraController carritoCompraController;

  @Test
  void shouldGetProductosDelCarritoSinStockDisponible() {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    SecretKey secretKey = MacProvider.generateKey();
    Claims claims =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(
                Jwts.builder()
                    .setIssuedAt(Date.from(zdtNow.toInstant()))
                    .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .claim("idUsuario", 1L)
                    .claim("roles", Collections.singletonList(Rol.ADMINISTRADOR))
                    .compact())
            .getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    BusquedaFacturaVentaCriteria busquedaFacturaVentaCriteria =
        BusquedaFacturaVentaCriteria.builder().build();
    List<ProductoFaltanteDTO> faltantes = new ArrayList<>();
    ProductoFaltanteDTO productoFaltante = new ProductoFaltanteDTO();
    productoFaltante.setIdProducto(1L);
    faltantes.add(productoFaltante);
    when(carritoCompraService.getProductosDelCarritoSinStockDisponible(1L, 1L)).thenReturn(faltantes);
    assertEquals(
        faltantes, carritoCompraController.getProductosDelCarritoSinStockDisponible(1L, "headers"));
  }
}
