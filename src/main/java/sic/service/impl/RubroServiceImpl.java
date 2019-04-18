package sic.service.impl;

import java.util.List;
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
import sic.modelo.Rubro;
import sic.service.IRubroService;
import sic.service.BusinessServiceException;
import sic.modelo.TipoDeOperacion;
import sic.repository.RubroRepository;

@Service
@Validated
public class RubroServiceImpl implements IRubroService {

    private final RubroRepository rubroRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RubroServiceImpl(RubroRepository rubroRepository) {
        this.rubroRepository = rubroRepository;
    }
    
    @Override
    public Rubro getRubroPorId(Long idRubro){
        Rubro rubro = rubroRepository.findById(idRubro);
        if (rubro == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_rubro_no_existente"));
        }
        return rubro;
    }

    @Override
    public List<Rubro> getRubros(Empresa empresa) {
        return rubroRepository.findAllByAndEmpresaAndEliminadoOrderByNombreAsc(empresa, false);
    }

    @Override
    public Rubro getRubroPorNombre(String nombre, Empresa empresa) {
        return rubroRepository.findByNombreAndEmpresaAndEliminado(nombre, empresa, false);
    }

  private void validarOperacion(TipoDeOperacion operacion, Rubro rubro) {
    // Duplicados
    // Nombre
    Rubro rubroDuplicado = this.getRubroPorNombre(rubro.getNombre(), rubro.getEmpresa());
    if (operacion.equals(TipoDeOperacion.ALTA) && rubroDuplicado != null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_rubro_nombre_duplicado"));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
        && (rubroDuplicado != null && rubroDuplicado.getId_Rubro() != rubro.getId_Rubro())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_rubro_nombre_duplicado"));
    }
  }

    @Override
    @Transactional
    public void actualizar(Rubro rubro) {
        this.validarOperacion(TipoDeOperacion.ACTUALIZACION, rubro);
        rubroRepository.save(rubro);
    }

    @Override
    @Transactional
    public Rubro guardar(@Valid Rubro rubro) {
        this.validarOperacion(TipoDeOperacion.ALTA, rubro);
        rubro = rubroRepository.save(rubro);
        LOGGER.warn("El Rubro {} se guardó correctamente.", rubro);
        return rubro;
    }

    @Override
    @Transactional
    public void eliminar(long idRubro) {
        Rubro rubro = this.getRubroPorId(idRubro);
        if (rubro == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_pedido_no_existente"));
        }
        rubro.setEliminado(true);
        rubroRepository.save(rubro);
    }
}
