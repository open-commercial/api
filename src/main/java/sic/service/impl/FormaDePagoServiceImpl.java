package sic.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.service.IFormaDePagoService;
import sic.service.BusinessServiceException;
import sic.repository.FormaDePagoRepository;

@Service
@Validated
public class FormaDePagoServiceImpl implements IFormaDePagoService {

  private final FormaDePagoRepository formaDePagoRepository;

  @Autowired
  public FormaDePagoServiceImpl(FormaDePagoRepository formaDePagoRepository) {
    this.formaDePagoRepository = formaDePagoRepository;
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
    Optional<FormaDePago> formaDePago = formaDePagoRepository
      .findById(idFormaDePago);
    if (formaDePago.isPresent() && !formaDePago.get().isEliminada()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
        ResourceBundle.getBundle("Mensajes")
          .getString("mensaje_formaDePago_no_existente"));
    }
  }

  @Override
  public FormaDePago getFormasDePagoPorId(long idFormaDePago) {
    Optional<FormaDePago> formaDePago = formaDePagoRepository.findById(idFormaDePago);
    if (formaDePago.isPresent()) {
      return formaDePago.get();
    } else {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_formaDePago_no_existente"));
    }
  }

  @Override
  public FormaDePago getFormaDePagoPredeterminada(Empresa empresa) {
    FormaDePago formaDePago =
        formaDePagoRepository.findByAndPredeterminadoAndEliminada(true, false);
    if (formaDePago == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_formaDePago_sin_predeterminada"));
    }
    return formaDePago;
  }
  //
  @Override
  @Transactional
  public void setFormaDePagoPredeterminada(FormaDePago formaDePago) {
    // antes de setear como predeterminado, busca si ya existe
    // otro como predeterminado y cambia su estado.
    FormaDePago formaPredeterminadaAnterior =
        formaDePagoRepository.findByAndPredeterminadoAndEliminada(true, false);
    if (formaPredeterminadaAnterior != null) {
      formaPredeterminadaAnterior.setPredeterminado(false);
      formaDePagoRepository.save(formaPredeterminadaAnterior);
    }
    formaDePago.setPredeterminado(true);
    formaDePagoRepository.save(formaDePago);
  }

  private void validarOperacion(FormaDePago formaDePago) {
    // Duplicados
    // Nombre
    if (formaDePagoRepository.findByNombreAndEliminada(formaDePago.getNombre(), false) != null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_formaDePago_duplicado_nombre"));
    }
    // Predeterminado
    if (formaDePago.isPredeterminado()
        && (formaDePagoRepository.findByAndPredeterminadoAndEliminada(true, false) != null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_formaDePago_predeterminada_existente"));
    }
  }
}
