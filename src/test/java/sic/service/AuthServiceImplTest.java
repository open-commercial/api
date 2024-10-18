package sic.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import sic.modelo.Rol;
import sic.modelo.TokenAccesoExcluido;
import sic.repository.TokenAccesoExcluidoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AuthServiceImpl.class, MessageSource.class})
@TestPropertySource(locations = "classpath:application.properties")
class AuthServiceImplTest {

  @MockBean TokenAccesoExcluidoRepository tokenAccesoExcluidoRepository;
  @MockBean MessageSource messageSource;
  @MockBean RestTemplate restTemplate;

  @Autowired AuthServiceImpl authService;
  static final String BEARER_TOKEN_PREFIX = "Bearer";

  @Test
  void shouldGenerarJWT() {
    var token = authService.generarJWT(1L, List.of(Rol.VENDEDOR));
    assertTrue(!token.isEmpty() && !token.isBlank());
  }

  @Test
  void shouldEsAuthorizationHeaderValido() {
    when(tokenAccesoExcluidoRepository.findByToken(anyString())).thenReturn(null);
    var token = authService.generarJWT(1L, List.of(Rol.ADMINISTRADOR));
    assertTrue(authService.esAuthorizationHeaderValido(BEARER_TOKEN_PREFIX + " " + token));
    verify(tokenAccesoExcluidoRepository).findByToken(anyString());
  }

  @Test
  void shouldExcluirTokenAcceso() {
    var token = anyString();
    var tae = TokenAccesoExcluido.builder().token(token).build();
    when(tokenAccesoExcluidoRepository.findByToken(token)).thenReturn(null);
    when(tokenAccesoExcluidoRepository.save(tae)).thenReturn(tae);
    authService.excluirTokenAcceso(BEARER_TOKEN_PREFIX + " " + anyString());
    verify(tokenAccesoExcluidoRepository).findByToken(token);
    verify(tokenAccesoExcluidoRepository).save(tae);
  }
}
