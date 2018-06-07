package sic.service;

import sic.modelo.*;
import org.springframework.data.domain.Page;

public interface IClienteService {

  void actualizar(Cliente cliente, Long idUsuarioCrendencial, long idUsuarioLoggedIn);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente, long idUsuarioLoggedIn);

  Cliente getClientePorId(long idCliente);

  Cliente getClientePorIdFiscal(String idFiscal, Empresa empresa);

  Cliente getClientePorRazonSocial(String razonSocial, Empresa empresa);

  Cliente getClientePredeterminado(Empresa empresa);

  boolean existeClientePredeterminado(Empresa empresa);

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(Cliente cliente, Long idUsuarioCrendencial, long idUsuarioLoggedIn);

  void validarOperacion(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuarioYidEmpresa(long idUsuario, Empresa empresa);
}
