package org.opencommercial.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.model.NotaCredito;
import org.opencommercial.model.NotaDebito;
import org.opencommercial.model.Usuario;
import org.opencommercial.model.dto.NuevaNotaCreditoSinFacturaDTO;
import org.opencommercial.model.dto.NuevaNotaDebitoSinReciboDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotaController.class, MessageSource.class})
class NotaControllerTest {

  @MockitoBean NotaService notaService;
  @MockitoBean ReciboService reciboService;
  @MockitoBean SucursalService sucursalService;
  @MockitoBean UsuarioService usuarioService;
  @MockitoBean AuthService authService;
  @MockitoBean MessageSource messageSource;

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
