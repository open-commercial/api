package sic.service.impl;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.builder.ClienteBuilder;
import sic.builder.EmpresaBuilder;
import sic.builder.LocalidadBuilder;
import sic.modelo.Cliente;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.ClienteRepository;

@ExtendWith(SpringExtension.class)
class ClienteServiceImplTest {

  @Mock private ClienteRepository clienteRepository;

  @InjectMocks private ClienteServiceImpl clienteServiceImpl;

  @Test
  void shouldSetClientePredeterminado() {
    Cliente resultadoEsperado = new ClienteBuilder().build();
    clienteServiceImpl.setClientePredeterminado(resultadoEsperado);
    when(clienteRepository.findByEmpresaAndPredeterminadoAndEliminado(
            (new EmpresaBuilder()).build(), true, false))
        .thenReturn((new ClienteBuilder()).build());
    Cliente resultadoObtenido =
        clienteServiceImpl.getClientePredeterminado((new EmpresaBuilder()).build());
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldLanzarExceptionWhenEmailInvalido() {
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () ->
                clienteServiceImpl.validarOperacion(
                    TipoDeOperacion.ELIMINACION, new ClienteBuilder().withEmail("@@.com").build()));
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_email_invalido")));
  }

  @Test
  void shouldLanzarExceptionWhenCondicionIVAesNull() {
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () ->
                clienteServiceImpl.validarOperacion(
                    TipoDeOperacion.ELIMINACION,
                    new ClienteBuilder()
                        .withCategoriaIVA(null)
                        .withEmail("soporte@gmail.com")
                        .withNombreFiscal("Ferreteria Julian")
                        .build()));
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_vacio_categoriaIVA")));
  }

  @Test
  void shouldLanzarExceptionWhenEmpresaEsNull() {
    BusinessServiceException thrown =
      assertThrows(
        BusinessServiceException.class,
        () ->
          clienteServiceImpl.validarOperacion(
            TipoDeOperacion.ELIMINACION,
            new ClienteBuilder()
              .withEmail("soporte@gmail.com")
              .withNombreFiscal("Ferreteria Julian")
              .withEmpresa(null)
              .build()));
    assertTrue(
      thrown
        .getMessage()
        .contains(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_cliente_vacio_empresa")));
  }

  @Test
  void shouldLanzarExceptionWhenIdFiscalDuplicadoEnAlta() {
    Cliente clienteNuevo = new ClienteBuilder().build();
    Cliente clienteDuplicado = new ClienteBuilder().build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(clienteRepository.findByIdFiscalAndEmpresaAndEliminado(
                      clienteNuevo.getIdFiscal(), clienteNuevo.getEmpresa(), false))
                  .thenReturn(clienteNuevo);
              clienteServiceImpl.validarOperacion(TipoDeOperacion.ALTA, clienteDuplicado);
            });
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_duplicado_idFiscal")));
  }

  @Test
  void shouldLanzarExceptionWhenIdFiscalDuplicadoEnActualizacion() {
    Cliente clienteNuevo =
        new ClienteBuilder()
            .withId_Cliente(7L)
            .withNombreFiscal("Merceria los dos botones")
            .withIdFiscal(23111111119L)
            .build();
    Cliente clienteDuplicado =
        new ClienteBuilder()
            .withId_Cliente(2L)
            .withNombreFiscal("Merceria los dos botones")
            .withIdFiscal(23111111119L)
            .build();
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(clienteRepository.findByIdFiscalAndEmpresaAndEliminado(
                      clienteNuevo.getIdFiscal(), clienteNuevo.getEmpresa(), false))
                  .thenReturn(clienteNuevo);
              clienteServiceImpl.validarOperacion(TipoDeOperacion.ACTUALIZACION, clienteDuplicado);
            });
    assertTrue(
        thrown
            .getMessage()
            .contains(
                ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_cliente_duplicado_idFiscal")));
  }
}
