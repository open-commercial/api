package org.opencommercial.service;

import java.util.List;
import org.opencommercial.model.FormaDePago;
import org.opencommercial.model.FormaDePagoEnum;

public interface FormaDePagoService {

  FormaDePago getFormaDePagoPredeterminada();

  List<FormaDePago> getFormasDePago();

  List<FormaDePago> getFormasDePagoNoEliminadas();

  FormaDePago getFormasDePagoNoEliminadoPorId(long id);

  FormaDePago getFormasDePagoPorId(long id);

  FormaDePago getFormaDePagoPorNombre(FormaDePagoEnum formaDePagoEnum);

  void setFormaDePagoPredeterminada(FormaDePago formaDePago);
}
