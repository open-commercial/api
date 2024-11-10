package org.opencommercial.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.model.CategoriaIVA;
import org.opencommercial.model.dto.RegistracionClienteAndUsuarioDTO;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegistracionServiceImpl.class, CustomValidator.class})
class RegistracionServiceImplTest {

  @MockBean UsuarioService usuarioService;
  @MockBean ClienteService clienteService;
  @MockBean EmailServiceFactory emailServiceFactory;

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
