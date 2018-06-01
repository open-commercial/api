package sic.service;

import sic.modelo.*;

import java.util.List;
import org.springframework.data.domain.Page;

public interface IClienteService {

    void actualizar(Cliente cliente);
        
    Page<Cliente> buscarClientes(BusquedaClienteCriteria criteria, long  idUsuario);

    void eliminar(long idCliente);

    Cliente getClientePorId(long id_Cliente);

    Cliente getClientePorIdFiscal(String idFiscal, Empresa empresa);

    Cliente getClientePorRazonSocial(String razonSocial, Empresa empresa);

    Cliente getClientePredeterminado(Empresa empresa);
    
    boolean existeClientePredeterminado(Empresa empresa);

    Cliente guardar(Cliente cliente);

    void setClientePredeterminado(Cliente cliente);

    void validarOperacion(TipoDeOperacion operacion, Cliente cliente);
    
    Cliente getClientePorIdPedido(long idPedido);

    Cliente getClientePorIdUsuario(long idUsuario);
}
