package sic.service;

import sic.modelo.*;
import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaClienteCriteria;
import sic.modelo.embeddable.ClienteEmbeddable;

import javax.validation.Valid;

public interface IClienteService {

  Cliente actualizar(@Valid Cliente clientePorActualizar, Cliente clientePersistido);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente);

  Cliente getClienteNoEliminadoPorId(long idCliente);

  Cliente getClientePredeterminado();

  boolean existeClientePredeterminado();

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(@Valid Cliente cliente);

  void validarOperacion(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuario(long idUsuario);

  Cliente getClientePorCredencial(Usuario usuarioCredencial);

  int desvincularClienteDeViajante(long idUsuarioViajante);

  int desvincularClienteDeCredencial(long idUsuarioCliente);

  String generarNroDeCliente();

  ClienteEmbeddable crearClienteEmbedded(Cliente cliente);
}
