package sic.service;

import java.util.List;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.FormaDePagoEnum;

public interface IFormaDePagoService {

  FormaDePago getFormaDePagoPredeterminada(Empresa empresa);

  List<FormaDePago> getFormasDePago(Empresa empresa);

  List<FormaDePago> getFormasDePagoNoEliminadas(Empresa empresa);

  FormaDePago getFormasDePagoNoEliminadoPorId(long id);

  FormaDePago getFormasDePagoPorId(long id);

  FormaDePago getFormaDePagoPorNombre(FormaDePagoEnum formaDePagoEnum);

  void setFormaDePagoPredeterminada(FormaDePago formaDePago);
}
