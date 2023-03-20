package sic.service;

import sic.entity.Cliente;
import sic.domain.TipoDeOperacion;
import sic.entity.Usuario;
import org.springframework.data.domain.Page;
import sic.entity.criteria.BusquedaClienteCriteria;
import sic.entity.embeddable.ClienteEmbeddable;

public interface IClienteService {

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
