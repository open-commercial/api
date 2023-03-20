package sic.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.domain.FormaDePago;
import sic.service.IFormaDePagoService;
import sic.repository.FormaDePagoRepository;

@Service
public class FormaDePagoServiceImpl implements IFormaDePagoService {

  private final FormaDePagoRepository formaDePagoRepository;
  private final MessageSource messageSource;

  @Autowired
  public FormaDePagoServiceImpl(
      FormaDePagoRepository formaDePagoRepository, MessageSource messageSource) {
    this.formaDePagoRepository = formaDePagoRepository;
    this.messageSource = messageSource;
  }

  @Override
  public List<sic.entity.FormaDePago> getFormasDePago() {
    return formaDePagoRepository.findAllByOrderByNombreAsc();
  }

  @Override
  public List<sic.entity.FormaDePago> getFormasDePagoNoEliminadas() {
    return formaDePagoRepository.findAllByAndEliminadaOrderByNombreAsc(false);
  }

  @Override
  public sic.entity.FormaDePago getFormasDePagoNoEliminadoPorId(long idFormaDePago) {
    Optional<sic.entity.FormaDePago> formaDePago = formaDePagoRepository.findById(idFormaDePago);
    if (formaDePago.isPresent() && !formaDePago.get().isEliminada()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_formaDePago_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public sic.entity.FormaDePago getFormasDePagoPorId(long idFormaDePago) {
    Optional<sic.entity.FormaDePago> formaDePago = formaDePagoRepository.findById(idFormaDePago);
    if (formaDePago.isPresent()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_formaDePago_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public sic.entity.FormaDePago getFormaDePagoPorNombre(FormaDePago formaDePagoEnum) {
    Optional<sic.entity.FormaDePago> formaDePago =
        formaDePagoRepository.findByNombreAndEliminada(formaDePagoEnum.toString(), false);
    if (formaDePago.isPresent() && !formaDePago.get().isEliminada()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_formaDePago_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public sic.entity.FormaDePago getFormaDePagoPredeterminada() {
    Optional<sic.entity.FormaDePago> formaDePago =
        formaDePagoRepository.findByAndPredeterminadoAndEliminada(true, false);
    if (formaDePago.isPresent()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage(
              "mensaje_formaDePago_sin_predeterminada", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public void setFormaDePagoPredeterminada(sic.entity.FormaDePago formaDePago) {
    Optional<sic.entity.FormaDePago> formaPredeterminadaAnterior =
        formaDePagoRepository.findByAndPredeterminadoAndEliminada(true, false);
    if (formaPredeterminadaAnterior.isPresent()) {
      formaPredeterminadaAnterior.get().setPredeterminado(false);
      formaDePagoRepository.save(formaPredeterminadaAnterior.get());
    }
    formaDePago.setPredeterminado(true);
    formaDePagoRepository.save(formaDePago);
  }
}
