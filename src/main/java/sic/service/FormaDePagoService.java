package sic.service;

import java.util.List;
import sic.modelo.FormaDePago;
import sic.modelo.FormaDePagoEnum;

public interface FormaDePagoService {

  FormaDePago getFormaDePagoPredeterminada();

  List<FormaDePago> getFormasDePago();

  List<FormaDePago> getFormasDePagoNoEliminadas();

  FormaDePago getFormasDePagoNoEliminadoPorId(long id);

  FormaDePago getFormasDePagoPorId(long id);

  FormaDePago getFormaDePagoPorNombre(FormaDePagoEnum formaDePagoEnum);

  void setFormaDePagoPredeterminada(FormaDePago formaDePago);
}
