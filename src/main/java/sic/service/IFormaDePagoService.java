package sic.service;

import java.util.List;
import sic.domain.FormaDePago;

public interface IFormaDePagoService {

  sic.entity.FormaDePago getFormaDePagoPredeterminada();

  List<sic.entity.FormaDePago> getFormasDePago();

  List<sic.entity.FormaDePago> getFormasDePagoNoEliminadas();

  sic.entity.FormaDePago getFormasDePagoNoEliminadoPorId(long id);

  sic.entity.FormaDePago getFormasDePagoPorId(long id);

  sic.entity.FormaDePago getFormaDePagoPorNombre(FormaDePago formaDePago);

  void setFormaDePagoPredeterminada(sic.entity.FormaDePago formaDePago);
}
