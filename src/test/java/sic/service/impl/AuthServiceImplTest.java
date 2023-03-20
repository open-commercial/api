package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import sic.entity.TokenAccesoExcluido;
import sic.repository.TokenAccesoExcluidoRepository;

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
  static final String BEARER_TOKEN_PREFIX = "Bearer ";

  @Value("${SIC_JWT_TOKEN}")
  private String jwtToken;

  @Test
  void shouldEsAuthorizationHeaderValido() {
    when(tokenAccesoExcluidoRepository.findByToken(jwtToken)).thenReturn(null);
    assertTrue(authService.esAuthorizationHeaderValido(BEARER_TOKEN_PREFIX + jwtToken));
    verify(tokenAccesoExcluidoRepository).findByToken(jwtToken);
  }

  @Test
  void shouldExcluirTokenAcceso() {
    TokenAccesoExcluido tae = new TokenAccesoExcluido();
    tae.setToken(jwtToken);
    when(tokenAccesoExcluidoRepository.findByToken(jwtToken)).thenReturn(null);
    when(tokenAccesoExcluidoRepository.save(tae)).thenReturn(tae);
    authService.excluirTokenAcceso(BEARER_TOKEN_PREFIX + jwtToken);
    verify(tokenAccesoExcluidoRepository).findByToken(anyString());
    verify(tokenAccesoExcluidoRepository).save(tae);
  }
}
