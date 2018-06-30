package sic.service;

import sic.modelo.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IClienteService {

  void actualizar(Cliente cliente);

  Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long idUsuario);

  void eliminar(long idCliente);

  Cliente getClientePorId(long idCliente);

  Cliente getClientePorIdFiscal(String idFiscal, Empresa empresa);

  Cliente getClientePorRazonSocial(String razonSocial, Empresa empresa);

  Cliente getClientePredeterminado(Empresa empresa);

  boolean existeClientePredeterminado(Empresa empresa);

  void setClientePredeterminado(Cliente cliente);

  Cliente guardar(Cliente cliente);

  void validarOperacion(TipoDeOperacion operacion, Cliente cliente);

  Cliente getClientePorIdPedido(long idPedido);

  Cliente getClientePorIdUsuarioYidEmpresa(long idUsuario, Empresa empresa);

  int desvincularClienteDeViajante(long idViajante);

  int desvincularClienteDeComprador(long idCliente);

}
