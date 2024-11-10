package org.opencommercial.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.Cliente;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.Usuario;
import org.opencommercial.repository.ClienteRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {ClienteServiceImpl.class, CustomValidator.class, MessageSource.class})
class ClienteServiceImplTest {

  @MockBean MessageSource messageSource;
  @MockBean ClienteRepository clienteRepository;
  @MockBean CuentaCorrienteService cuentaCorrienteService;
  @MockBean UsuarioService usuarioService;
  @MockBean UbicacionService ubicacionService;

  @Autowired ClienteServiceImpl clienteService;

  String mensaje_cliente_duplicado_idFiscal = "Ya existe el ID fiscal ingresado.";

  @BeforeEach
  void setup() {
    when(messageSource.getMessage("mensaje_cliente_duplicado_idFiscal", null, Locale.getDefault()))
        .thenReturn(mensaje_cliente_duplicado_idFiscal);
  }

  @Test
  void shouldSetClientePredeterminado() {
    Cliente resultadoEsperado = new Cliente();
    clienteService.setClientePredeterminado(resultadoEsperado);
    when(clienteRepository.findByAndPredeterminadoAndEliminado(true, false))
        .thenReturn(resultadoEsperado);
    Cliente resultadoObtenido = clienteService.getClientePredeterminado();
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
    when(clienteRepository.findByIdFiscalAndEliminado(clienteNuevo.getIdFiscal(), false))
        .thenReturn(listaClienteNuevo);
    assertThrows(
        BusinessServiceException.class,
        () -> clienteService.validarReglasDeNegocio(TipoDeOperacion.ALTA, clienteDuplicado));
    verify(messageSource).getMessage(eq("mensaje_cliente_duplicado_idFiscal"), any(), any());
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
    when(clienteRepository.findByIdFiscalAndEliminado(clienteNuevo.getIdFiscal(), false))
            .thenReturn(listaClienteNuevo);
    assertThrows(
        BusinessServiceException.class,
        () -> clienteService.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, clienteDuplicado));
    verify(messageSource).getMessage(eq("mensaje_cliente_duplicado_idFiscal"), any(), any());
  }

  @Test
  void shouldThrowExceptionWhenNroClienteDuplicado() {
    Cliente cliente = new Cliente();
    cliente.setNroCliente("123567");
    Usuario usuario = new Usuario();
    cliente.setCredencial(usuario);
    when(clienteRepository.existsByNroCliente("123567")).thenReturn(true);
    assertThrows(
        BusinessServiceException.class,
        () -> clienteService.validarReglasDeNegocio(TipoDeOperacion.ALTA, cliente));
    verify(messageSource).getMessage(eq("mensaje_cliente_duplicado_nro"), any(), any());
  }

}
