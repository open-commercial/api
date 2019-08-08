package sic.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.FormaDePagoEnum;
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
  public List<FormaDePago> getFormasDePago(Empresa empresa) {
    return formaDePagoRepository.findAllByOrderByNombreAsc();
  }

  @Override
  public List<FormaDePago> getFormasDePagoNoEliminadas(Empresa empresa) {
    return formaDePagoRepository.findAllByAndEliminadaOrderByNombreAsc(false);
  }

  @Override
  public FormaDePago getFormasDePagoNoEliminadoPorId(long idFormaDePago) {
    Optional<FormaDePago> formaDePago = formaDePagoRepository.findById(idFormaDePago);
    if (formaDePago.isPresent() && !formaDePago.get().isEliminada()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_formaDePago_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public FormaDePago getFormasDePagoPorId(long idFormaDePago) {
    Optional<FormaDePago> formaDePago = formaDePagoRepository.findById(idFormaDePago);
    if (formaDePago.isPresent()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_formaDePago_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public FormaDePago getFormaDePagoPorNombre(FormaDePagoEnum formaDePagoEnum) {
    Optional<FormaDePago> formaDePago =
        formaDePagoRepository.findByNombreAndEliminada(formaDePagoEnum.toString(), false);
    if (formaDePago.isPresent() && !formaDePago.get().isEliminada()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_formaDePago_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public FormaDePago getFormaDePagoPredeterminada(Empresa empresa) {
    Optional<FormaDePago> formaDePago =
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
  public void setFormaDePagoPredeterminada(FormaDePago formaDePago) {
    // antes de setear como predeterminado, busca si ya existe
    // otro como predeterminado y cambia su estado.
    Optional<FormaDePago> formaPredeterminadaAnterior =
        formaDePagoRepository.findByAndPredeterminadoAndEliminada(true, false);
    if (formaPredeterminadaAnterior.isPresent()) {
      formaPredeterminadaAnterior.get().setPredeterminado(false);
      formaDePagoRepository.save(formaPredeterminadaAnterior.get());
    }
    formaDePago.setPredeterminado(true);
    formaDePagoRepository.save(formaDePago);
  }
}
