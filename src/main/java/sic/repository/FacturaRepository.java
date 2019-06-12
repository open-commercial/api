package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Factura;
import sic.modelo.Pedido;

public interface FacturaRepository<T extends Factura> extends PagingAndSortingRepository<T, Long> {
    
    List<Factura> findAllByPedidoAndEliminada(Pedido pedido, boolean eliminada);
    
}
