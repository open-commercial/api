package org.opencommercial.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.model.Usuario;
import org.opencommercial.repository.UsuarioRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {UsuarioServiceImpl.class, CustomValidator.class, MessageSource.class})
class UsuarioServiceImplTest {

  @MockBean UsuarioRepository usuarioRepository;
  @MockBean MessageSource messageSource;

  @Autowired UsuarioServiceImpl usuarioService;

  @Test
  void shouldGetUsuarioPorPasswordRecoveryKeyAndIdUsuario() {
    Usuario usuario = new Usuario();
    usuario.setPasswordRecoveryKeyExpirationDate(LocalDateTime.MAX);
    when(usuarioRepository.findByPasswordRecoveryKeyAndIdUsuarioAndEliminadoAndHabilitado(
            anyString(), anyLong()))
        .thenReturn(usuario);
    Usuario u = usuarioService.getUsuarioPorPasswordRecoveryKeyAndIdUsuario("testRecoveryKey", 1L);
    assertNotNull(u);
    assertFalse(LocalDateTime.now().isAfter(usuario.getPasswordRecoveryKeyExpirationDate()));
  }

  @Test
  void shouldEsUsuarioHabilitado() {
    Usuario usuario = new Usuario();
    usuario.setHabilitado(true);
    when(usuarioRepository.findByIdUsuario(anyLong())).thenReturn(Optional.of(usuario));
    assertTrue(usuarioService.esUsuarioHabilitado(1L));
  }
}
