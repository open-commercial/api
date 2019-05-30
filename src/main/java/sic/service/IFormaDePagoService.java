package sic.service;

import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;

import javax.validation.Valid;

public interface IFormaDePagoService {

    void eliminar(long idFormaDePago);

    FormaDePago getFormaDePagoPredeterminada(Empresa empresa);

    List<FormaDePago> getFormasDePago(Empresa empresa);

    FormaDePago getFormasDePagoNoEliminadoPorId(long id);

    FormaDePago guardar(@Valid FormaDePago formaDePago);

    void setFormaDePagoPredeterminada(FormaDePago formaDePago);
    
}
