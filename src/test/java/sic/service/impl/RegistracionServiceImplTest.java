package sic.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.CategoriaIVA;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;
import sic.service.IClienteService;
import sic.service.ICorreoElectronicoService;
import sic.service.IUsuarioService;
import sic.util.CustomValidator;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegistracionServiceImpl.class, CustomValidator.class})
class RegistracionServiceImplTest {

  @MockBean IUsuarioService usuarioService;
  @MockBean IClienteService clienteService;
  @MockBean ICorreoElectronicoService correoElectronicoService;

  @Autowired RegistracionServiceImpl registracionService;

  @Test
  void shouldThrowExceptionWhenCrearCuenta() {
    ConstraintViolationException ex = assertThrows(
        ConstraintViolationException.class,
        () -> {
          RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO =
              RegistracionClienteAndUsuarioDTO.builder()
                  .apellido("¬{{[]]}}}][]]]")
                  .nombre("|@@#~~½¬")
                  .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
                  .email("sansagot.com")
                  .telefono("123")
                  .password("corto")
                  .recaptcha("111111")
                  .nombreFiscal("theRedWolf")
                  .build();
          registracionService.crearCuenta(registracionClienteAndUsuarioDTO);
        });
    assertEquals(5, ex.getConstraintViolations().size());
  }
}
