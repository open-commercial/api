package sic.service;

import java.util.List;
import sic.modelo.FormaDePago;

public interface IFormaDePagoService {

    FormaDePago getFormaDePagoPredeterminada();

    List<FormaDePago> getFormasDePago();

    List<FormaDePago> getFormasDePagoNoEliminadas();

    FormaDePago getFormasDePagoNoEliminadoPorId(long id);

    FormaDePago getFormasDePagoPorId(long id);

    void setFormaDePagoPredeterminada(FormaDePago formaDePago);
    
}
