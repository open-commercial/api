package sic.service;

import sic.modelo.*;
import org.springframework.data.domain.Page;

import javax.validation.Valid;

public interface IClienteService {

  void actualizar(@Valid Cliente clientePorActualizar, Cliente clientePersistido);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente);

  Cliente getClienteNoEliminadoPorId(long idCliente);

  Cliente getClientePorIdFiscal(Long idFiscal, Sucursal sucursal);

  Cliente getClientePredeterminado(Sucursal sucursal);

  boolean existeClientePredeterminado(Sucursal sucursal);

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(@Valid Cliente cliente);

  void validarOperacion(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuarioYidSucursal(long idUsuario, long idSucursal);

  Cliente getClientePorCredencial(Usuario usuarioCredencial);

  int desvincularClienteDeViajante(long idUsuarioViajante);

  int desvincularClienteDeCredencial(long idUsuarioCliente);

  String generarNroDeCliente(Sucursal sucursal);
}
