package sic.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.ItemCarritoCompra;

public interface ICarritoCompraService {

    BigDecimal getTotal(long idUsuario);

    BigDecimal getCantArticulos(long idUsuario);
    
    long getCantRenglones(long idUsuario);

    Page<ItemCarritoCompra> getAllItemsDelUsuario(long idUsuario, Pageable pageable);

    void eliminarItem(long idUsuario, long idProducto);

    void eliminarTodosLosItems(long idUsuario);

    void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad);

}
