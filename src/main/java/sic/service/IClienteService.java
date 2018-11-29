package sic.service;

import sic.modelo.*;
import org.springframework.data.domain.Page;

public interface IClienteService {

  void actualizar(Cliente clientePorActualizar, Cliente clientePersistido);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente);

  Cliente getClientePorId(long idCliente);

  Cliente getClientePorIdFiscal(Long idFiscal, Empresa empresa);

  Cliente getClientePredeterminado(Empresa empresa);

  boolean existeClientePredeterminado(Empresa empresa);

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(Cliente cliente);

  void validarOperacion(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuarioYidEmpresa(long idUsuario, long idEmpresa);

  Cliente getClientePorCredencial(Usuario usuarioCredencial);

  int desvincularClienteDeViajante(long idUsuarioViajante);

  int desvincularClienteDeCredencial(long idUsuarioCliente);

  String generarNroDeCliente(Empresa empresa);
}
