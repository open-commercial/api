package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaGastoCriteria;
import sic.modelo.Sucursal;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;

import javax.validation.Valid;

public interface IGastoService {
    
    Gasto getGastoNoEliminadoPorId(Long id);
    
    void eliminar(long idGasto);

    List<Gasto> getGastosEntreFechasYFormaDePago(Sucursal sucursal, FormaDePago formaDePago, Date desde, Date hasta);

    Gasto guardar(@Valid Gasto gasto);

    void validarOperacion(Gasto gasto);

    Page<Gasto> buscarGastos(BusquedaGastoCriteria criteria);

    long getUltimoNumeroDeGasto(long idSucursal);

    BigDecimal getTotalGastosEntreFechasYFormaDePago(long idSucursal, long idFormaDePago, Date desde, Date hasta);

    BigDecimal getTotalGastosQueAfectanCajaEntreFechas(long idSucursal, Date desde, Date hasta);

    BigDecimal getTotalGastosEntreFechas(long idSucursal, Date desde, Date hasta);

    BigDecimal getTotalGastos(BusquedaGastoCriteria criteria);

}
