package org.opencommercial.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.Rubro;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.repository.RubroRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class RubroServiceImpl implements RubroService {

  private final RubroRepository rubroRepository;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public RubroServiceImpl(
    RubroRepository rubroRepository,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.rubroRepository = rubroRepository;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public Rubro getRubroNoEliminadoPorId(Long idRubro) {
    Optional<Rubro> rubro = rubroRepository
      .findById(idRubro);
    if (rubro.isPresent() && !rubro.get().isEliminado()) {
      return rubro.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_rubro_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public List<Rubro> getRubros() {
    return rubroRepository.findAllByAndEliminadoOrderByNombreAsc(false);
  }

  @Override
  public Rubro getRubroPorNombre(String nombre) {
    return rubroRepository.findByNombreAndEliminado(nombre, false);
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Rubro rubro) {
    // Duplicados
    // Nombre
    Rubro rubroDuplicado = this.getRubroPorNombre(rubro.getNombre());
    if (operacion.equals(TipoDeOperacion.ALTA) && rubroDuplicado != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_rubro_nombre_duplicado", null, Locale.getDefault()));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
        && (rubroDuplicado != null && rubroDuplicado.getIdRubro() != rubro.getIdRubro())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_rubro_nombre_duplicado", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public void actualizar(Rubro rubro) {
    customValidator.validar(rubro);
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, rubro);
    rubroRepository.save(rubro);
  }

  @Override
  @Transactional
  public Rubro guardar(Rubro rubro) {
    customValidator.validar(rubro);
    this.validarReglasDeNegocio(TipoDeOperacion.ALTA, rubro);
    rubro = rubroRepository.save(rubro);
    log.info("El rubro se guardó correctamente. {}", rubro);
    return rubro;
  }

  @Override
  @Transactional
  public void eliminar(long idRubro) {
    Rubro rubro = this.getRubroNoEliminadoPorId(idRubro);
    if (rubro == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_pedido_no_existente", null, Locale.getDefault()));
    }
    rubro.setEliminado(true);
    rubroRepository.save(rubro);
  }
}
