package sic.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.service.AuthServiceImpl;
import sic.service.CarritoCompraServiceImpl;
import sic.service.ProductoServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CarritoCompraController.class, MessageSource.class})
class CarritoCompraControllerTest {

  @MockBean CarritoCompraServiceImpl carritoCompraService;
  @MockBean ProductoServiceImpl productoService;
  @MockBean AuthServiceImpl authService;
  @MockBean MessageSource messageSource;

  @Autowired CarritoCompraController carritoCompraController;

  @Test
  void shouldGetProductosDelCarritoSinStockDisponible() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    List<ProductoFaltanteDTO> faltantes = new ArrayList<>();
    ProductoFaltanteDTO productoFaltante = new ProductoFaltanteDTO();
    productoFaltante.setIdProducto(1L);
    faltantes.add(productoFaltante);
    when(carritoCompraService.getProductosDelCarritoSinStockDisponible(1L, 1L)).thenReturn(faltantes);
    assertEquals(
            faltantes,
            carritoCompraController.getProductosDelCarritoSinStockDisponible(1L, "headers"));
  }
}
