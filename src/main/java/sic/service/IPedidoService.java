package sic.service;

import java.util.HashMap;
import java.util.List;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaPedidoCriteria;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.Pedido;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonPedido;

public interface IPedidoService {

    Pedido getPedidoPorId(Long id);
    
    Pedido getPedidoPorNumeroYEmpresa(Long nroPedido, Empresa empresa);
            
    void actualizar(Pedido pedido);

    Page<Pedido> buscarConCriteria(BusquedaPedidoCriteria criteria);

    long calcularNumeroPedido(Empresa empresa);
    
    Pedido actualizarEstadoPedido(Pedido pedido, List<Factura> facturas);

    Pedido calcularTotalActualDePedido(Pedido pedido);

    boolean eliminar(long idPedido);

    List<Factura> getFacturasDelPedido(long id);

    HashMap<Long, RenglonFactura> getRenglonesFacturadosDelPedido(long nroPedido);

    List<RenglonPedido> getRenglonesDelPedido(Long idPedido);

    byte[] getReportePedido(Pedido pedido);

    Pedido guardar(Pedido pedido);

}
