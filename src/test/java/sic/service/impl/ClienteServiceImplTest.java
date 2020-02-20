package sic.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.modelo.Cliente;
import sic.exception.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.modelo.Usuario;
import sic.repository.ClienteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ExtendWith(SpringExtension.class)
class ClienteServiceImplTest {

  @Mock private MessageSource messageSource;
  @Mock private ClienteRepository clienteRepository;
  @InjectMocks private ClienteServiceImpl clienteServiceImpl;

  private String mensaje_cliente_duplicado_idFiscal = "Ya existe el ID fiscal ingresado.";

  @BeforeEach
  void setup() {
    when(messageSource.getMessage("mensaje_cliente_duplicado_idFiscal", null, Locale.getDefault()))
        .thenReturn(mensaje_cliente_duplicado_idFiscal);
  }

  @Test
  void shouldSetClientePredeterminado() {
    Cliente resultadoEsperado = new Cliente();
    clienteServiceImpl.setClientePredeterminado(resultadoEsperado);
    when(clienteRepository.findByAndPredeterminadoAndEliminado(true, false))
        .thenReturn(resultadoEsperado);
    Cliente resultadoObtenido = clienteServiceImpl.getClientePredeterminado();
    assertEquals(resultadoEsperado, resultadoObtenido);
  }

  @Test
  void shouldLanzarExceptionWhenIdFiscalDuplicadoEnAlta() {
    Cliente clienteNuevo = new Cliente();
    clienteNuevo.setIdFiscal(1234L);
    clienteNuevo.setCredencial(new Usuario());
    List<Cliente> listaClienteNuevo = new ArrayList<>();
    listaClienteNuevo.add(clienteNuevo);
    Cliente clienteDuplicado = new Cliente();
    clienteDuplicado.setIdFiscal(1234L);
    clienteDuplicado.setCredencial(new Usuario());
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(clienteRepository.findByIdFiscalAndEliminado(clienteNuevo.getIdFiscal(), false))
                  .thenReturn(listaClienteNuevo);
              clienteServiceImpl.validarOperacion(TipoDeOperacion.ALTA, clienteDuplicado);
            });
    assertTrue(thrown.getMessage().contains(mensaje_cliente_duplicado_idFiscal));
  }

  @Test
  void shouldLanzarExceptionWhenIdFiscalDuplicadoEnActualizacion() {
    List<Cliente> listaClienteNuevo = new ArrayList<>();
    Cliente clienteNuevo = new Cliente();
    clienteNuevo.setIdCliente(7L);
    clienteNuevo.setNombreFiscal("Merceria los dos botones");
    clienteNuevo.setIdFiscal(23111111119L);
    listaClienteNuevo.add(clienteNuevo);
    Cliente clienteDuplicado = new Cliente();
    clienteDuplicado.setIdCliente(2L);
    clienteDuplicado.setNombreFiscal("Merceria los dos botones");
    clienteDuplicado.setIdFiscal(23111111119L);
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> {
              when(clienteRepository.findByIdFiscalAndEliminado(clienteNuevo.getIdFiscal(), false))
                  .thenReturn(listaClienteNuevo);
              clienteServiceImpl.validarOperacion(TipoDeOperacion.ACTUALIZACION, clienteDuplicado);
            });
    assertTrue(thrown.getMessage().contains(mensaje_cliente_duplicado_idFiscal));
  }
}
