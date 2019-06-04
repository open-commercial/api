package sic.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public FormaDePagoServiceImpl(FormaDePagoRepository formaDePagoRepository) {
    this.formaDePagoRepository = formaDePagoRepository;
  }

  @Override
  public List<FormaDePago> getFormasDePago(Empresa empresa) {
    return formaDePagoRepository.findAllByAndEmpresaOrderByNombreAsc(empresa);
  }

  @Override
  public List<FormaDePago> getFormasDePagoNoEliminadas(Empresa empresa) {
    return formaDePagoRepository.findAllByAndEmpresaAndEliminadaOrderByNombreAsc(empresa, false);
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
  public FormaDePago getFormaDePagoPredeterminada(Empresa empresa) {
    FormaDePago formaDePago =
        formaDePagoRepository.findByAndEmpresaAndPredeterminadoAndEliminada(empresa, true, false);
    if (formaDePago == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_formaDePago_sin_predeterminada"));
    }
    return formaDePago;
  }

  @Override
  @Transactional
  public void setFormaDePagoPredeterminada(FormaDePago formaDePago) {
    // antes de setear como predeterminado, busca si ya existe
    // otro como predeterminado y cambia su estado.
    FormaDePago formaPredeterminadaAnterior =
        formaDePagoRepository.findByAndEmpresaAndPredeterminadoAndEliminada(
            formaDePago.getEmpresa(), true, false);
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
    if (formaDePagoRepository.findByNombreAndEmpresaAndEliminada(
            formaDePago.getNombre(), formaDePago.getEmpresa(), false)
        != null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_formaDePago_duplicado_nombre"));
    }
    // Predeterminado
    if (formaDePago.isPredeterminado()
        && (formaDePagoRepository.findByAndEmpresaAndPredeterminadoAndEliminada(
                formaDePago.getEmpresa(), true, false)
            != null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_forma_de_pago_empresa_vacia"));
    }
  }

  @Override
  @Transactional
  public FormaDePago guardar(@Valid FormaDePago formaDePago) {
    this.validarOperacion(formaDePago);
    formaDePago = formaDePagoRepository.save(formaDePago);
    logger.warn("La Forma de Pago {} se guardó correctamente.", formaDePago);
    return formaDePago;
  }

  @Override
  @Transactional
  public void eliminar(long idFormaDePago) {
    FormaDePago formaDePago = this.getFormasDePagoNoEliminadoPorId(idFormaDePago);
    if (formaDePago == null) {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_formaDePago_no_existente"));
    }
    formaDePago.setEliminada(true);
    formaDePagoRepository.save(formaDePago);
  }
}
