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
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.modelo.dto.NuevaNotaCreditoSinFacturaDTO;
import sic.modelo.dto.NuevaNotaDebitoSinReciboDTO;
import sic.service.*;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotaController.class, MessageSource.class})
public class NotaControllerTest {

  @MockBean INotaService notaService;
  @MockBean IReciboService reciboService;
  @MockBean ISucursalService sucursalService;
  @MockBean IUsuarioService usuarioService;
  @MockBean IAuthService authService;
  @MockBean MessageSource messageSource;

  @Autowired NotaController notaController;

  @Test
  void shouldGuardarNotaCreditoSinFactura() {
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
    Usuario usuario = new Usuario();
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFactura = NuevaNotaCreditoSinFacturaDTO.builder().build();
    NotaCredito nuevaNotaCredito = new NotaCredito();
    when(notaService.calcularNotaCreditoSinFactura(nuevaNotaCreditoSinFactura, usuario)).thenReturn(nuevaNotaCredito);
    notaController.guardarNotaCreditoSinFactura(nuevaNotaCreditoSinFactura, "headers");
    verify(notaService).guardarNotaCredito(nuevaNotaCredito);
  }

  @Test
  void shouldGuardarNotaDebitoSinRecibo() {
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
    Usuario usuario = new Usuario();
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinRecibo = NuevaNotaDebitoSinReciboDTO.builder().build();
    NotaDebito notaDebito = new NotaDebito();
    when(notaService.calcularNotaDebitoSinRecibo(nuevaNotaDebitoSinRecibo, usuario)).thenReturn(notaDebito);
    notaController.guardarNotaDebitoSinRecibo(nuevaNotaDebitoSinRecibo, "headers");
    verify(notaService).guardarNotaDebito(notaDebito);
  }
}
