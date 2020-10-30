package sic.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Rol;
import sic.modelo.Traspaso;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.service.impl.AuthServiceImpl;
import sic.service.impl.TraspasoServiceImpl;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TraspasoController.class})
class TraspasoControllerTest {

  @MockBean TraspasoServiceImpl traspasoService;
  @MockBean AuthServiceImpl authService;

  @Autowired TraspasoController traspasoController;

  @Test
  void shouldBuscarTraspasos() {
    BusquedaTraspasoCriteria traspasoCriteria = BusquedaTraspasoCriteria.builder().build();
    traspasoController.getTraspasosCriteria(traspasoCriteria);
    verify(traspasoService).buscarTraspasos(traspasoCriteria);
  }

  @Test
  void shouldGetRenglones() {
    traspasoController.getRenglonesDelTraspaso(1L);
    verify(traspasoService).getRenglonesTraspaso(1L);
  }

  @Test
  void shouldGuardarTraspaso() {
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
    traspasoController.guardarTraspaso(NuevoTraspasoDTO.builder().build(), "headers");
    verify(traspasoService).guardarTraspaso(NuevoTraspasoDTO.builder().build(), 1L);
  }

  @Test
  void shouldEliminarTraspaso() {
    when(traspasoService.getTraspasoNoEliminadoPorid(1L)).thenReturn(new Traspaso());
    traspasoController.eliminarTraspaso(1L);
    verify(traspasoService).eliminar(1L);
  }
}
