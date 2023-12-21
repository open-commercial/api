package sic.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Medida;
import sic.service.IMedidaService;
import sic.exception.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.MedidaRepository;
import sic.util.CustomValidator;

@Service
@Slf4j
public class MedidaServiceImpl implements IMedidaService {

  private final MedidaRepository medidaRepository;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public MedidaServiceImpl(
    MedidaRepository medidaRepository,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.medidaRepository = medidaRepository;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public Medida getMedidaNoEliminadaPorId(Long idMedida) {
    Optional<Medida> medida = medidaRepository
      .findById(idMedida);
    if (medida.isPresent() && !medida.get().isEliminada()) {
      return medida.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_medida_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public List<Medida> getUnidadMedidas() {
    return medidaRepository.findAllByAndEliminadaOrderByNombreAsc(false);
  }

  @Override
  public Medida getMedidaPorNombre(String nombre) {
    return medidaRepository.findByNombreAndEliminada(nombre, false);
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Medida medida) {
    // Duplicados
    // Nombre
    Medida medidaDuplicada = this.getMedidaPorNombre(medida.getNombre());
    if (operacion.equals(TipoDeOperacion.ALTA) && medidaDuplicada != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_medida_duplicada_nombre", null, Locale.getDefault()));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
        && medidaDuplicada != null
        && medidaDuplicada.getIdMedida() != medida.getIdMedida()) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_medida_duplicada_nombre", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public void actualizar(Medida medida) {
    customValidator.validar(medida);
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, medida);
    medidaRepository.save(medida);
  }

  @Override
  @Transactional
  public Medida guardar(Medida medida) {
    customValidator.validar(medida);
    this.validarReglasDeNegocio(TipoDeOperacion.ALTA, medida);
    medida = medidaRepository.save(medida);
    log.info("La Medida {} se guardó correctamente.", medida);
    return medida;
  }

  @Override
  @Transactional
  public void eliminar(long idMedida) {
    Medida medida = this.getMedidaNoEliminadaPorId(idMedida);
    if (medida == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_medida_no_existente", null, Locale.getDefault()));
    }
    medida.setEliminada(true);
    medidaRepository.save(medida);
  }
}
