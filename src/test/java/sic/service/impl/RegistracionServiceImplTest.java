package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.CategoriaIVA;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class RegistracionServiceImplTest {

  @InjectMocks RegistracionServiceImpl registracionService;

  @Test
  void shouldCrearCuenta() {
    RegistracionClienteAndUsuarioDTO registro =
            RegistracionClienteAndUsuarioDTO.builder()
                    .apellido("¬{{[]]}}}][]]]")
                    .nombre("|@@#~~½¬")
                    .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
                    .email("sansa@got.com")
                    .telefono("4157899667")
                    .password("caraDeMala")
                    .recaptcha("111111")
                    .nombreFiscal("theRedWolf")
                    .build();
    registracionService.crearCuenta(registro);
  }
}
