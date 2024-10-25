package sic.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.NotaCredito;
import sic.modelo.NotaDebito;
import sic.modelo.Usuario;
import sic.modelo.dto.NuevaNotaCreditoSinFacturaDTO;
import sic.modelo.dto.NuevaNotaDebitoSinReciboDTO;
import sic.service.*;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotaController.class, MessageSource.class})
class NotaControllerTest {

  @MockBean NotaService notaService;
  @MockBean ReciboService reciboService;
  @MockBean SucursalService sucursalService;
  @MockBean UsuarioService usuarioService;
  @MockBean AuthService authService;
  @MockBean MessageSource messageSource;

  @Autowired NotaController notaController;

  @Test
  void shouldGuardarNotaCreditoSinFactura() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
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
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
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
