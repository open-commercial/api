package org.opencommercial.service;

import org.opencommercial.model.Cliente;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.Usuario;
import org.opencommercial.model.criteria.BusquedaClienteCriteria;
import org.opencommercial.model.embeddable.ClienteEmbeddable;
import org.springframework.data.domain.Page;

public interface ClienteService {

  Cliente actualizar(Cliente clientePorActualizar, Cliente clientePersistido);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente);

  Cliente getClienteNoEliminadoPorId(long idCliente);

  Cliente getClientePredeterminado();

  boolean existeClientePredeterminado();

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(Cliente cliente);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuario(long idUsuario);

  Cliente getClientePorCredencial(Usuario usuarioCredencial);

  void desvincularClienteDeViajante(long idUsuarioViajante);

  void desvincularClienteDeCredencial(long idUsuarioCliente);

  String generarNroDeCliente();

  ClienteEmbeddable crearClienteEmbedded(Cliente cliente);
}
