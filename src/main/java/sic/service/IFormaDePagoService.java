package sic.service;

import java.util.List;
import sic.modelo.Sucursal;
import sic.modelo.FormaDePago;

public interface IFormaDePagoService {

    FormaDePago getFormaDePagoPredeterminada(Sucursal sucursal);

    List<FormaDePago> getFormasDePago(Sucursal sucursal);

    List<FormaDePago> getFormasDePagoNoEliminadas(Sucursal sucursal);

    FormaDePago getFormasDePagoNoEliminadoPorId(long id);

    FormaDePago getFormasDePagoPorId(long id);

    void setFormaDePagoPredeterminada(FormaDePago formaDePago);
    
}
