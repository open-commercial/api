package sic.service;

import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;

public interface IFormaDePagoService {

    void eliminar(long idFormaDePago);

    FormaDePago getFormaDePagoPredeterminada(Empresa empresa);

    List<FormaDePago> getFormasDePago(Empresa empresa);

    FormaDePago getFormasDePagoPorId(long id);

    FormaDePago guardar(FormaDePago formaDePago);

    void setFormaDePagoPredeterminada(FormaDePago formaDePago);
    
}
