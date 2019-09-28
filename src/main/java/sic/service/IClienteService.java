package sic.service;

import sic.modelo.*;
import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaClienteCriteria;

import javax.validation.Valid;

public interface IClienteService {

  void actualizar(@Valid Cliente clientePorActualizar, Cliente clientePersistido);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente);

  Cliente getClienteNoEliminadoPorId(long idCliente);

  Cliente getClientePorIdFiscal(Long idFiscal, Empresa empresa);

  Cliente getClientePredeterminado(Empresa empresa);

  boolean existeClientePredeterminado(Empresa empresa);

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(@Valid Cliente cliente);

  void validarOperacion(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuarioYidEmpresa(long idUsuario, long idEmpresa);

  Cliente getClientePorCredencial(Usuario usuarioCredencial);

  int desvincularClienteDeViajante(long idUsuarioViajante);

  int desvincularClienteDeCredencial(long idUsuarioCliente);

  String generarNroDeCliente(Empresa empresa);
}
